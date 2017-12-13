package app

import app.util.Heroku
import org.eclipse.egit.github.core.Repository
import org.eclipse.egit.github.core.RepositoryCommit
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.service.RepositoryService
import org.eclipse.egit.github.core.service.UserService
import org.eclipse.egit.github.core.service.WatcherService
import java.io.Serializable
import java.util.*
import kotlin.streams.toList

object UserCtrl {

    // https://javadoc.io/doc/org.eclipse.mylyn.github/org.eclipse.egit.github.core/2.1.5

    val client = GitHubClient().apply {
        setOAuth2Token(Heroku.getOauthToken() ?: System.getProperty("oauth-token"))
    }

    private val repoService = RepositoryService(client)
    private val commitService = CommitService(client)
    private val userService = UserService(client)
    private val watcherService = WatcherService(client)
    private val githubProfileSummary = repoService.getRepository("tipsy", "github-profile-summary")

    fun getUserProfile(username: String): UserProfile {
        if (Cache.invalid(username)) {
            val user = userService.getUser(username)
            val repos = repoService.getRepositories(username).filter { !it.isFork && it.size != 0 }
            val repoCommits = repos.parallelStream().map { it to commitsForRepo(it).filter { it.author?.login == username } }.toList().toMap()
            val langRepoGrouping = repos.groupingBy { (it.language ?: "Unknown") }

            val quarterCommitCount = repoCommits.flatMap { it.value }.groupingBy { getYearAndQuarter(it) }.fold(0) { acc, _ -> acc + 1 }.toSortedMap()
            val langRepoCount = langRepoGrouping.eachCount().toSortedMap()
            val langStarCount = langRepoGrouping.fold(0) { acc, repo -> acc + repo.watchers }.toSortedMap()
            val langCommitCount = langRepoGrouping.fold(0) { acc, repo -> acc + repoCommits[repo]!!.size }.toSortedMap()
            val repoCommitCount = repoCommits.map { it.key.name to it.value.size }.toList().sortedBy { (_, v) -> -v }.take(10).toMap().toSortedMap()
            val repoStarCount = repos.filter { it.watchers > 0 }.map { it.name to it.watchers }.sortedBy { (_, v) -> -v }.take(10).toMap().toSortedMap()

            Cache.putUserProfile(UserProfile(
                    user,
                    quarterCommitCount,
                    langRepoCount,
                    langStarCount,
                    langCommitCount,
                    repoCommitCount,
                    repoStarCount
            ))
        }
        return Cache.getUserProfile(username)!!
    }

    fun hasStarredRepo(username: String?): Boolean {
        try {
            if (username.isNullOrEmpty()) {
                return false
            }
            if (Cache.contains(username)) {
                return true
            }
            val watchers = watcherService.getWatchers(githubProfileSummary).map { it.login }
            return watchers.contains(username)
        } catch (e: Exception) {
            return false
        }
    }

    private fun commitsForRepo(repo: Repository): List<RepositoryCommit> = try {
        commitService.getCommits(repo)
    } catch (e: Exception) {
        listOf()
    }

    private fun getYearAndQuarter(it: RepositoryCommit): String {
        val date = it.commit.committer.date
        return "${(1900 + date.year)}-Q${date.getMonth() / 3 + 1}"
    }

}

data class UserProfile(
        val user: User,
        val quarterCommitCount: Map<String, Int>,
        val langRepoCount: Map<String, Int>,
        val langStarCount: Map<String, Int>,
        val langCommitCount: Map<String, Int>,
        val repoCommitCount: Map<String, Int>,
        val repoStarCount: Map<String, Int>
) : Serializable {
    val timeStamp = Date().time
}
