package app

import app.util.Heroku
import app.util.RateLimitUtil
import io.javalin.Javalin
import io.javalin.core.util.Header
import io.javalin.embeddedserver.jetty.websocket.WsSession
import io.javalin.translator.template.TemplateUtil.model
import org.apache.commons.lang.StringEscapeUtils.escapeHtml
import org.eclipse.egit.github.core.client.RequestException
import org.slf4j.LoggerFactory
import java.util.*

fun main(args: Array<String>) {

    val log = LoggerFactory.getLogger("app.MainKt")

    val unrestricted = Config.getUnrestrictedState()?.toBoolean() ?: false

    val app = Javalin.create().apply {
        port(Config.getPort() ?: 7070)
        enableStandardRequestLogging()
        enableDynamicGzip()
    }

    app.apply { // add routes

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
            ws.onConnect { session -> Timer().scheduleAtFixedRate(reportRemainingRequests(session), 0, 1000) }
        }

    }

    app.apply { // add exception/error handlers

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
            if (ctx.header(Header.ACCEPT)?.contains("application/json") == false) {
                ctx.redirect("/search")
            }
        }

    }

    RateLimitUtil.enableTerribleRateLimiting(app)
    Heroku.enableSslRedirect(app)

    app.start()

}

private fun reportRemainingRequests(session: WsSession) = object : TimerTask() {
    override fun run() {
        if (session.isOpen) {
            return session.send(GhService.remainingRequests.toString())
        }
        this.cancel()
    }
}
