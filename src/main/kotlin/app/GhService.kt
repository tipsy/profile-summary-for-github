package app

import io.javalin.embeddedserver.jetty.websocket.WsSession
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.service.RepositoryService
import org.eclipse.egit.github.core.service.UserService
import org.eclipse.egit.github.core.service.WatcherService
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object GhService {

    // https://javadoc.io/doc/org.eclipse.mylyn.github/org.eclipse.egit.github.core/2.1.5

    private val log = LoggerFactory.getLogger(GhService.javaClass)

    private val tokens = Config.getApiTokens()?.split(",") ?: listOf("") // empty token is limited to 60 requests
    private val clients = tokens.map { GitHubClient().apply { setOAuth2Token(it) } }
    private val repoServices = clients.map { RepositoryService(it) }
    private val commitServices = clients.map { CommitService(it) }
    private val userServices = clients.map { UserService(it) }
    private val watcherServices = clients.map { WatcherService(it) }
    private val colorsByLanguage = ConcurrentHashMap<String, String>()

    val repos: RepositoryService get() = repoServices.maxBy { it.client.remainingRequests }!!
    val commits: CommitService get() = commitServices.maxBy { it.client.remainingRequests }!!
    val users: UserService get() = userServices.maxBy { it.client.remainingRequests }!!
    val watchers: WatcherService get() = watcherServices.maxBy { it.client.remainingRequests }!!

    val remainingRequests: Int get() = clients.sumBy { it.remainingRequests }

    // Allows for parallel iteration and O(1) put/remove
    private val clientSessions = ConcurrentHashMap<WsSession, Boolean>()

    fun registerClient(ws: WsSession) = clientSessions.put(ws, true) == true
    fun unregisterClient(ws: WsSession) = clientSessions.remove(ws) == true

    fun getLangColor(language: String): String? = colorsByLanguage[language]

    init {
        Executors.newScheduledThreadPool(2).apply {

            // ping clients every other minute to make sure remainingRequests is correct
            scheduleAtFixedRate({
                repoServices.forEach {
                    try {
                        it.getRepository("tipsy", "github-profile-summary")
                        log.info("Pinged client ${clients.indexOf(it.client)} - client.remainingRequests was ${it.client.remainingRequests}")
                    } catch (e: Exception) {
                        log.info("Pinged client ${clients.indexOf(it.client)} - was rate-limited")
                    }
                }
            }, 0, 2, TimeUnit.MINUTES)

            // update all connected clients with remainingRequests twice per second
            scheduleAtFixedRate({
                val remainingRequests = remainingRequests.toString()
                clientSessions.forEachKey(1) {
                    try {
                        it.send(remainingRequests)
                    } catch (e: Exception) {
                        log.error(e.toString())
                    }
                }
            }, 0, 500, TimeUnit.MILLISECONDS)

            // fetch and store the github language colors in the background
            submit({
                val endpoint = "https://raw.githubusercontent.com/github/linguist/master/lib/linguist/languages.yml"
                val data = URL(endpoint).openStream().bufferedReader().use { it.readText() }

                val yaml = Yaml().load(data) as Map<String, Map<String, Any>>
                yaml.forEach({ lang, info ->
                    val color = info["color"]
                    if (color != null && color is String && color.length == 7 && color.startsWith('#')) {
                        colorsByLanguage[lang] = color
                    }
                })

                if (colorsByLanguage.isEmpty())
                    log.warn("GhService colorsByLanguage is empty!")
            })

        }
    }

}
