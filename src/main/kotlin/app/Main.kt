package app

import app.util.Heroku
import io.javalin.Javalin
import io.javalin.translator.template.TemplateUtil.model
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {

    val log = LoggerFactory.getLogger("app.MainKt")

    val app = Javalin.create().apply {
        port(Heroku.getPort() ?: 7070)
        enableDynamicGzip()
    }.start()

    app.get("/api/user/:user") { ctx ->
        ctx.json(UserCtrl.getUserProfile(ctx.param("user")!!))
    }

    app.get("/user/:user") { ctx ->
        val user = ctx.param("user")!!
        if (UserCtrl.userExists(user)) {
            ctx.renderVelocity("user.vm", model("user", user))
        } else {
            ctx.redirect("/search?q=$user")
        }
    }

    app.get("/search") { ctx ->
        val user = ctx.queryParam("q")
        if (UserCtrl.userExists(user)) {
            ctx.redirect("/user/$user")
        } else {
            ctx.renderVelocity("search.vm", model("q", ctx.queryParam("q") ?: ""));
        }
    }

    app.error(404) { ctx -> ctx.redirect("/search") }

    app.exception(Exception::class.java) { e, ctx ->
        log.warn("Uncaught exception", e)
        ctx.status(500)
    }

}



