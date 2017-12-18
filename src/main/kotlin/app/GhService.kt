package app

import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.service.RepositoryService
import org.eclipse.egit.github.core.service.UserService
import org.eclipse.egit.github.core.service.WatcherService
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

object GhService {

    private val log = LoggerFactory.getLogger(GhService.javaClass)

    private val tokens = Config.getApiTokens()?.split(",") ?: listOf("") // empty token is limited to 60 requests
    private val clients = tokens.map { token -> GitHubClient().apply { setOAuth2Token(token) } }
    private val repoServices = clients.map { RepositoryService(it) }
    private val commitServices = clients.map { CommitService(it) }
    private val userServices = clients.map { UserService(it) }
    private val watcherServices = clients.map { WatcherService(it) }

    val repos: RepositoryService get() = repoServices.maxBy { it.client.remainingRequests }!!
    val commits: CommitService get() = commitServices.maxBy { it.client.remainingRequests }!!
    val users: UserService get() = userServices.maxBy { it.client.remainingRequests }!!
    val watchers: WatcherService get() = watcherServices.maxBy { it.client.remainingRequests }!!

    val remainingRequests: Int get() = clients.sumBy { it.remainingRequests }

    init { // create timer to ping clients every other minute to make sure remainingRequests is correct
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                repoServices.forEach { it.getRepository("tipsy", "github-profile-summary") }
                repoServices.forEach { log.info("Pinged client ${clients.indexOf(it.client)} - client.remainingRequests was ${it.client.remainingRequests}") }
            }
        }, 0, TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES))
    }

}

