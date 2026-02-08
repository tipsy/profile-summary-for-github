package app

import app.util.CommitCountUtil
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.RepositoryCommit
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.IntStream
import kotlin.streams.toList

object UserService {

    private val log = LoggerFactory.getLogger("app.UserCtrlKt")

    fun userExists(user: String): Boolean = try {
        GhService.users.getUser(user) != null
    } catch (e: Exception) {
        false
    }

    private fun remainingRequests(): Int = GhService.remainingRequests

    // Lightweight check that doesn't consume GitHub API requests
    // Used by /api/can-load to check before showing the spinner
    fun canLoadUserQuick(user: String): Boolean {
        val userCacheJson = CacheService.selectJsonFromDb(user)
        return Config.unrestricted()
            || (userCacheJson != null)
            || remainingRequests() > 0
    }

    fun getUserIfCanLoad(username: String): UserProfile? {
        val userCacheJson = CacheService.selectJsonFromDb(username)
        val canLoadUser = Config.unrestricted()
            || (userCacheJson != null)
            || remainingRequests() > 0

        if (canLoadUser) {
            return if (userCacheJson == null) {
                generateUserProfile(username)
            } else {
                CacheService.getUserFromJson(userCacheJson)
            }
        }

        return null
    }

    private fun commitsForRepo(repo: Repository): List<RepositoryCommit> = try {
        GhService.commits.getCommits(repo)
    } catch (e: Exception) {
        listOf()
    }

    private fun generateUserProfile(username: String): UserProfile {
        val user = GhService.users.getUser(username)
        val repos = GhService.repos.getRepositories(username).filter { !it.isFork && it.size != 0 }
        val repoCommits = repos.parallelStream().map { it to commitsForRepo(it).filter { it.author?.login.equals(username, ignoreCase = true) } }.toList().toMap()
        val langRepoGrouping = repos.groupingBy { (it.language ?: "Unknown") }

        val quarterCommitCount = CommitCountUtil.getCommitsForQuarters(user, repoCommits)
        val langRepoCount = langRepoGrouping.eachCount().toList().sortedBy { (_, v) -> -v }.toMap()
        val langStarCount = langRepoGrouping.fold(0) { acc, repo -> acc + repo.watchers }.toList().filter { (_, v) -> v > 0 }.sortedBy { (_, v) -> -v }.toMap()
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

        CacheService.saveInCache(userProfile)

        return userProfile;
    }
}
