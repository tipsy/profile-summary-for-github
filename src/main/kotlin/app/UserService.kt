package app

import app.util.CommitCountUtil
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.RepositoryCommit
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.UserPlan
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.sql.DriverManager
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.IntStream
import kotlin.streams.toList

object UserService {

    private const val pageSize = 100
    private const val urlToDb = "jdbc:h2:mem:userinfo"
    private val log = LoggerFactory.getLogger("app.UserCtrlKt")
    private val repo = GhService.repos.getRepository("tipsy", "profile-summary-for-github")
    private val watchers = ConcurrentHashMap.newKeySet<String>()
    private val freeRequestCutoff = Config.freeRequestCutoff()

    fun userExists(user: String): Boolean = try {
        GhService.users.getUser(user) != null
    } catch (e: Exception) {
        false
    }

    fun canLoadUser(user: String): Boolean {
        val remainingRequests by lazy { GhService.remainingRequests }
        val hasFreeRemainingRequests by lazy { remainingRequests > (freeRequestCutoff ?: remainingRequests) }
        return Config.unrestricted()
                || (lookUpInCache(user) != null)
                || hasFreeRemainingRequests
                || (remainingRequests > 0 && hasStarredRepo(user))
    }

    fun getUserProfile(username: String): UserProfile {
        return (lookUpInCache(username) ?: generateUserProfile(username))
    }

    private fun hasStarredRepo(username: String): Boolean {
        val login = username.toLowerCase()
        if (watchers.contains(login)) return true
        syncWatchers()
        return watchers.contains(login)
    }

    fun syncWatchers() {
        val realWatchers = repo.watchers
        if (watchers.size < realWatchers) {
            val startPage = watchers.size / pageSize + 1
            val lastPage = realWatchers / pageSize + 1
            if (startPage == lastPage)
                addAllWatchers(lastPage)
            else
                IntStream.rangeClosed(startPage, lastPage).parallel().forEach { page -> addAllWatchers(page) }
        }
    }

    private fun addAllWatchers(pageNumber: Int) = try {
        GhService.watchers.pageWatchers(repo, pageNumber, pageSize).first().forEach { watchers.add(it.login.toLowerCase()) }
    } catch (e: Exception) {
        log.info("Exception while adding watchers", e)
    }

    private fun commitsForRepo(repo: Repository): List<RepositoryCommit> = try {
        GhService.commits.getCommits(repo)
    } catch (e: Exception) {
        listOf()
    }

    private fun createTableIfAbsent() {
        val connection = DriverManager.getConnection(urlToDb)
        val statement = connection.createStatement()

        statement.execute(
        "CREATE TABLE IF NOT EXISTS userinfo (" +
            "id VARCHAR2 PRIMARY KEY," +
            "timestamp TIMESTAMP WITH TIME ZONE, " +
            "data JSON" +
            ")"
        )
    }

    private fun lookUpInCache(username: String): UserProfile? {
        val connection = DriverManager.getConnection(urlToDb)

        createTableIfAbsent()

        val preparedStatement = connection.prepareStatement(
        "SELECT " +
            "timestamp, " +
            "data " +
            "FROM userinfo " +
            "WHERE id = ?"
        )
        preparedStatement.setString(1, username)

        val result = preparedStatement.executeQuery()
        result.use {
            // guaranteed to be at most one.
            if (it.next()) {
                val timestamp = it.getTimestamp(1).toLocalDateTime()
                val diffInHours = ChronoUnit.HOURS.between(timestamp, LocalDateTime.now())
                if (diffInHours <= 6) {
                    val json: String = it.getString(2)

                    log.debug("cache hit: {}", json)

                    val simpleModule = SimpleModule()
                    simpleModule.addDeserializer(UserProfile::class.java, UserProfile.Deserializer())

                    val objectMapper = jacksonObjectMapper()
                    objectMapper.registerModule(simpleModule)

                    return objectMapper.readValue<UserProfile>(json)
                }
            }
        }

        log.debug("cache miss for username: {}", username)

        return null
    }

    private fun saveInCache(userProfile: UserProfile) {
        val connection = DriverManager.getConnection(urlToDb)

        createTableIfAbsent()

        val json = jacksonObjectMapper().writeValueAsString(userProfile)

        val preparedStatement = connection.prepareStatement(
        "MERGE INTO userinfo (id, timestamp, data) KEY (id) " +
            "VALUES (?, CURRENT_TIMESTAMP(), ? FORMAT JSON)"
        )

        preparedStatement.setString(1, userProfile.user.login)
        preparedStatement.setString(2, json)

        preparedStatement.execute()
    }

    private fun generateUserProfile(username: String): UserProfile {
        val user = GhService.users.getUser(username)
        val repos = GhService.repos.getRepositories(username).filter { !it.isFork && it.size != 0 }
        val repoCommits = repos.parallelStream().map { it to commitsForRepo(it).filter { it.author?.login.equals(username, ignoreCase = true) } }.toList().toMap()
        val langRepoGrouping = repos.groupingBy { (it.language ?: "Unknown") }

        val quarterCommitCount = CommitCountUtil.getCommitsForQuarters(user, repoCommits)
        val langRepoCount = langRepoGrouping.eachCount().toList().sortedBy { (_, v) -> -v }.toMap()
        val langStarCount = langRepoGrouping.fold(0) { acc, repo -> acc + repo.watchers }.toList().sortedBy { (_, v) -> -v }.toMap()
        val langCommitCount = langRepoGrouping.fold(0) { acc, repo -> acc + repoCommits[repo]!!.size }.toList().sortedBy { (_, v) -> -v }.toMap()
        val repoCommitCount = repoCommits.map { it.key.name to it.value.size }.toList().sortedBy { (_, v) -> -v }.take(10).toMap()
        val repoStarCount = repos.filter { it.watchers > 0 }.map { it.name to it.watchers }.sortedBy { (_, v) -> -v }.take(10).toMap()

        val repoCommitCountDescriptions = repoCommitCount.map { it.key to repos.find { r -> r.name == it.key }?.description }.toMap()
        val repoStarCountDescriptions = repoStarCount.map { it.key to repos.find { r -> r.name == it.key }?.description }.toMap()

        val userProfile = UserProfile(
            user,
            quarterCommitCount,
            langRepoCount,
            langStarCount,
            langCommitCount,
            repoCommitCount,
            repoStarCount,
            repoCommitCountDescriptions,
            repoStarCountDescriptions
        )

        saveInCache(userProfile)

        return userProfile;
    }
}

data class UserProfile(
        val user: User,
        val quarterCommitCount: Map<String, Int>,
        val langRepoCount: Map<String, Int>,
        val langStarCount: Map<String, Int>,
        val langCommitCount: Map<String, Int>,
        val repoCommitCount: Map<String, Int>,
        val repoStarCount: Map<String, Int>,
        val repoCommitCountDescriptions: Map<String, String?>,
        val repoStarCountDescriptions: Map<String, String?>
) : Serializable {
    val timeStamp = Instant.now().toEpochMilli()

    class Deserializer(valueClass: Class<*>?): StdDeserializer<UserProfile>(valueClass) {
        constructor(): this(null)

        override fun deserialize(jsonParser: JsonParser?, context: DeserializationContext?): UserProfile {
            if (jsonParser == null || context == null) {
                throw RuntimeException()
            }

            val userProfileNode: JsonNode = jsonParser.codec?.readTree(jsonParser) ?: throw RuntimeException()

            val userNode = userProfileNode.get("user")
            val user = User()
            user.isHireable = userNode.booleanValue()
            user.createdAt = Date(userNode.longValue())
            user.collaborators = userNode.intValue()
            user.diskUsage = userNode.intValue()
            user.followers = userNode.intValue()
            user.following = userNode.intValue()
            user.id = userNode.intValue()
            user.ownedPrivateRepos = userNode.intValue()
            user.privateGists = userNode.intValue()
            user.publicGists = userNode.intValue()
            user.publicRepos = userNode.intValue()
            user.totalPrivateRepos = userNode.intValue()
            user.avatarUrl = userNode.textValue()
            user.blog = userNode.textValue()
            user.company = userNode.textValue()
            user.email = userNode.textValue()
            user.gravatarId = userNode.textValue()
            user.htmlUrl = userNode.textValue()
            user.location = userNode.textValue()
            user.login = userNode.textValue()
            user.name = userNode.textValue()
            user.type = userNode.textValue()
            user.url = userNode.textValue()

            val planNode = userNode.get("plan")
            user.plan = if (planNode.isNull) null else context.readValue(planNode.traverse(), UserPlan::class.java)

            val quarterCommitCount: MutableMap<String, Int> = parseInt(userProfileNode.get("quarterCommitCount"))
            val langRepoCount: MutableMap<String, Int> = parseInt(userProfileNode.get("langRepoCount"))
            val langStarCount: MutableMap<String, Int> = parseInt(userProfileNode.get("langStarCount"))
            val langCommitCount: MutableMap<String, Int> = parseInt(userProfileNode.get("langCommitCount"))
            val repoCommitCount: MutableMap<String, Int> = parseInt(userProfileNode.get("repoCommitCount"))
            val repoStarCount: MutableMap<String, Int> = parseInt(userProfileNode.get("repoStarCount"))
            val repoCommitCountDescriptions: MutableMap<String, String?> = parseString(userProfileNode.get("repoCommitCountDescriptions"))
            val repoStarCountDescriptions: MutableMap<String, String?> = parseString(userProfileNode.get("repoStarCountDescriptions"))

            return UserProfile(
                user,
                quarterCommitCount,
                langRepoCount,
                langStarCount,
                langCommitCount,
                repoCommitCount,
                repoStarCount,
                repoCommitCountDescriptions,
                repoStarCountDescriptions
            )
        }

        private fun parseInt(jsonNode: JsonNode): MutableMap<String, Int> {
            val map: MutableMap<String, Int> = mutableMapOf()
            if (!jsonNode.isNull) {
                val fieldNames = jsonNode.fieldNames()
                while (fieldNames.hasNext()) {
                    val key = fieldNames.next()
                    val value = jsonNode[key].intValue()
                    map[key] = value
                }
            }

            return map
        }

        private fun parseString(jsonNode: JsonNode): MutableMap<String, String?> {
            val map: MutableMap<String, String?> = mutableMapOf()
            if (!jsonNode.isNull) {
                val fieldNames = jsonNode.fieldNames()
                while (fieldNames.hasNext()) {
                    val key = fieldNames.next()
                    val value = jsonNode[key].textValue()
                    map[key] = value
                }
            }

            return map
        }
    }
}
