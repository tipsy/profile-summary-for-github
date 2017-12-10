package app

import app.util.Heroku
import org.eclipse.egit.github.core.RepositoryCommit
import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.service.RepositoryService
import org.eclipse.egit.github.core.service.UserService
import java.io.Serializable
import java.util.*
import kotlin.streams.toList

object UserCtrl {

    // https://javadoc.io/doc/org.eclipse.mylyn.github/org.eclipse.egit.github.core/2.1.5

    private val client = GitHubClient().apply {
        setOAuth2Token(Heroku.getOauthToken() ?: System.getenv("OAUTH_TOKEN"))
    }

    private val repoService = RepositoryService(client)
    private val commitService = CommitService(client)
    private val userService = UserService(client)

    fun getUserProfile(username: String): UserProfile {
        if (Cache.invalid(username)) {
            val user = userService.getUser(username)
            val repos = repoService.getRepositories(username).filter { !it.isFork && it.size != 0 }
            val repoCommits = repos.parallelStream().map { it to commitService.getCommits(it).filter { it.author?.login == username } }.toList().toMap()
            val langRepoGrouping = repos.groupingBy { (it.language ?: "Unknown") }

            val quarterCommitCount = repoCommits.flatMap { it.value }.groupingBy { getYearAndQuarter(it) }.fold(0) { acc, _ -> acc + 1 }.toSortedMap()
            val langRepoCount = langRepoGrouping.eachCount()
            val langStarCount = langRepoGrouping.fold(0) { acc, repo -> acc + repo.watchers }
            val langCommitCount = langRepoGrouping.fold(0) { acc, repo -> acc + repoCommits[repo]!!.size }
            val repoCommitCount = repoCommits.map { it.key.name to it.value.size }.toList().sortedBy { (_, v) -> -v }.take(50).toMap()

            Cache.putUserProfile(UserProfile(user, quarterCommitCount, langRepoCount, langStarCount, langCommitCount, repoCommitCount))
        }
        return Cache.getUserProfile(username)!!
    }

    fun userExists(username: String?) = try {
        Cache.getUserProfile(username ?: "") != null || userService.getUser(username) != null
    } catch (e: Exception) {
        false
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
        val repoCommitCount: Map<String, Int>
) : Serializable {
    val timeStamp = Date().time
}
