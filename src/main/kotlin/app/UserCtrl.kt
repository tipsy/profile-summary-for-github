package app

import app.util.CommitCountUtil
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.RepositoryCommit
import org.eclipse.egit.github.core.User
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.streams.toList

object UserCtrl {

    private val log = LoggerFactory.getLogger("app.UserCtrlKt")

    private val repo = GhService.repos.getRepository("tipsy", "profile-summary-for-github")
    private val watchers = ConcurrentHashMap.newKeySet<String>()

    fun getUserProfile(username: String): UserProfile {
        if (Cache.invalid(username)) {
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

            Cache.putUserProfile(UserProfile(
                    user,
                    quarterCommitCount,
                    langRepoCount,
                    langStarCount,
                    langCommitCount,
                    repoCommitCount,
                    repoStarCount,
                    repoCommitCountDescriptions,
                    repoStarCountDescriptions
            ))
        }
        return Cache.getUserProfile(username)!!
    }

    fun hasStarredRepo(username: String): Boolean {
        addAllWatchers(pageNumber = watchers.size / 100 + 1)
        return watchers.find { it == username.toLowerCase() } != null
    }

    fun initWatcherSet() {
        val lastPage = repo.watchers / 100 + 1
        (0..lastPage).toList().parallelStream().forEach { page -> addAllWatchers(page) }
    }

    private fun addAllWatchers(pageNumber: Int) = try {
        watchers.addAll(GhService.watchers.pageWatchers(repo, pageNumber, 100).first().map { it.login.toLowerCase() })
    } catch (e: Exception) {
        log.info("Exception while adding watchers", e)
    }

    private fun commitsForRepo(repo: Repository): List<RepositoryCommit> = try {
        GhService.commits.getCommits(repo)
    } catch (e: Exception) {
        listOf()
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
}
