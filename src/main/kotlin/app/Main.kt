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
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.slf4j.LoggerFactory
import java.util.*

fun main(args: Array<String>) {

    val log = LoggerFactory.getLogger("app.MainKt")

    val unrestricted = Config.getUnrestrictedState()?.toBoolean() ?: false

    val app = Javalin.create().apply {
        port(Config.getPort() ?: 7070)
        enableStandardRequestLogging()
        enableDynamicGzip()
        embeddedServer(EmbeddedJettyFactory {
            Server(QueuedThreadPool(200, 8, 120000))
        })
    }

    // add routes
    app.apply {

        get("/api/user/:user") { ctx ->
            val user = ctx.param("user")!!
            when (UserCtrl.hasStarredRepo(user) || unrestricted) {
                true -> ctx.json(UserCtrl.getUserProfile(ctx.param("user")!!))
                false -> ctx.status(400)
            }
        }

        get("/user/:user") { ctx ->
            val user = ctx.param("user")!!
            when (UserCtrl.hasStarredRepo(user) || unrestricted) {
                true -> ctx.renderVelocity("user.vm", model("user", user))
                false -> ctx.redirect("/search?q=$user")
            }
        }

        get("/search") { ctx ->
            val user = ctx.queryParam("q")?.trim() ?: ""
            when (UserCtrl.hasStarredRepo(user) || (unrestricted && user != "")) {
                true -> ctx.redirect("/user/$user")
                false -> ctx.renderVelocity("search.vm", model("q", escapeHtml(user)))
            }
        }

        ws("/rate-limit-status") { ws ->
            ws.onConnect { session ->
                Timer().scheduleAtFixedRate(GhService.broadcastRemainingRequests(session), 0, 1000)
            }
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

}
