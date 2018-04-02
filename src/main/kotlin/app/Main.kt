package app

import app.util.HerokuUtil
import app.util.RateLimitUtil
import io.javalin.Javalin
import io.javalin.core.util.Header
import io.javalin.embeddedserver.jetty.EmbeddedJettyFactory
import io.javalin.translator.template.TemplateUtil.model
import org.apache.commons.lang.StringEscapeUtils.escapeHtml
import org.eclipse.egit.github.core.client.RequestException
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {

    val log = LoggerFactory.getLogger("app.MainKt")

    val gtmId = Config.getGtmId()

    fun canLoadUser(user: String): Boolean {
        val unrestricted = Config.getUnrestrictedState()?.toBoolean() == true
        val remainingRequests by lazy { GhService.remainingRequests }
        val hasFreeRemainingRequests by lazy { remainingRequests > (Config.freeRequestCutoff() ?: remainingRequests) }
        return unrestricted
                || Cache.contains(user)
                || hasFreeRemainingRequests
                || UserCtrl.hasStarredRepo(user)
    }

    val app = Javalin.create().apply {
        enableStandardRequestLogging()
        enableDynamicGzip()
        embeddedServer(EmbeddedJettyFactory {
            Server(QueuedThreadPool(200, 8, 120000)).apply {
                connectors = arrayOf(ServerConnector(server).apply {
                    port = Config.getPort() ?: 7070
                    idleTimeout = 120_000
                })
            }
        })
    }

    // add routes
    app.apply {

        get("/api/user/:user") { ctx ->
            val user = ctx.param("user")!!
            when (canLoadUser(user)) {
                true -> ctx.json(UserCtrl.getUserProfile(ctx.param("user")!!))
                false -> ctx.status(400)
            }
        }

        get("/user/:user") { ctx ->
            val user = ctx.param("user")!!
            when (canLoadUser(user)) {
                true -> ctx.renderVelocity("user.vm", model("user", user, "gtmId", gtmId))
                false -> ctx.redirect("/search?q=$user")
            }
        }

        get("/search") { ctx ->
            val user = ctx.queryParam("q")?.trim() ?: ""
            when (user != "" && canLoadUser(user)) {
                true -> ctx.redirect("/user/$user")
                false -> ctx.renderVelocity("search.vm", model("q", escapeHtml(user), "gtmId", gtmId))
            }
        }

        ws("/rate-limit-status") { ws ->
            ws.onConnect { session -> GhService.registerClient(session) }
            ws.onClose { session, _, _ -> GhService.unregisterClient(session) }
            ws.onError { session, _ -> GhService.unregisterClient(session) }
        }

    }

    // add exception/error handlers
    app.apply {

        exception(Exception::class.java) { e, ctx ->
            log.warn("Uncaught exception", e)
            ctx.status(500)
        }

        exception(RequestException::class.java) { e, ctx ->
            if (e.message == "Not Found (404)") {
                ctx.status(404)
            }
        }

        error(404) { ctx ->
            if (ctx.header(Header.ACCEPT)?.contains("application/json") == false || !ctx.response().isCommitted) {
                ctx.redirect("/search")
            }
        }

    }

    RateLimitUtil.enableTerribleRateLimiting(app)
    HerokuUtil.enableSslRedirect(app)

    app.start()

    UserCtrl.initWatcherSet()

}
