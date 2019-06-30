package app

import app.util.HerokuUtil
import app.util.RateLimitUtil
import io.javalin.Javalin
import io.javalin.core.util.Header
import io.javalin.http.BadRequestResponse
import io.javalin.http.NotFoundResponse
import io.javalin.plugin.rendering.vue.VueComponent
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.slf4j.LoggerFactory

fun main() {

    val log = LoggerFactory.getLogger("app.MainKt")
    val app = Javalin.create {
        it.addStaticFiles("/public")
        it.server {
            Server(QueuedThreadPool(200, 8, 120000)).apply {
                connectors = arrayOf(ServerConnector(server).apply {
                    port = Config.getPort() ?: 7070
                    idleTimeout = 120_000
                })
            }
        }
    }.apply {
        get("/api/can-load") { ctx ->
            val user = ctx.queryParam<String>("user").get()
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
    }.error(404) {
        if (it.header(Header.ACCEPT)?.contains("text/html") == true) it.redirect("/search")
    }.start()

    HerokuUtil.enableSslRedirect(app)
    RateLimitUtil.enableTerribleRateLimiting(app)
    UserService.syncWatchers()

}
