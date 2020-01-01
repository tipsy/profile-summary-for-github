package app

import io.javalin.Javalin
import io.javalin.core.compression.Brotli
import io.javalin.core.compression.Gzip
import io.javalin.http.BadRequestResponse
import io.javalin.http.NotFoundResponse
import io.javalin.http.util.RateLimit
import io.javalin.plugin.rendering.vue.VueComponent
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

fun main() {

    val log = LoggerFactory.getLogger("app.MainKt")
    val app = Javalin.create {
        it.enforceSsl = true
        it.addStaticFiles("/public")
        it.compressionStrategy(Brotli(), Gzip())
        it.server {
            Server(QueuedThreadPool(200, 8, 120000)).apply {
                connectors = arrayOf(ServerConnector(server).apply {
                    port = Config.getPort() ?: 7070
                    idleTimeout = 120_000
                })
            }
        }
    }.apply {
        before("/api/*") { RateLimit(it).requestPerTimeUnit(20, TimeUnit.MINUTES) }
        get("/api/can-load") { ctx ->
            val user = ctx.queryParam<String>("user").get()
            if (!UserService.userExists(user)) throw NotFoundResponse()
            ctx.status(if (UserService.canLoadUser(user)) 200 else 400)
        }
        get("/api/user/:user") { ctx ->
            val user = ctx.pathParam("user")
            if (!UserService.userExists(user)) throw NotFoundResponse()
            if (!UserService.canLoadUser(user)) throw BadRequestResponse("Can't load user")
            ctx.json(UserService.getUserProfile(user))
        }
        get("/search", VueComponent("<search-view></search-view>"))
        get("/user/:user", VueComponent("<user-view></user-view>"))
        ws("/rate-limit-status") { ws ->
            ws.onConnect { GhService.registerClient(it) }
            ws.onClose { GhService.unregisterClient(it) }
            ws.onError { GhService.unregisterClient(it) }
        }
        after { it.cookie("gtm-id", Config.getGtmId() ?: "") }
    }.exception(Exception::class.java) { e, ctx ->
        log.warn("Uncaught exception", e)
        ctx.status(500)
    }.error(404, "html") {
        it.redirect("/search")
    }.start()

    UserService.syncWatchers()

}
