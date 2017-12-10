package app

import app.util.Heroku
import io.javalin.Javalin
import io.javalin.translator.template.TemplateUtil.model

fun main(args: Array<String>) {

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
            ctx.redirect("/search?q=$user&notfound")
        }
    }

    app.get("*") { ctx ->
        val user = ctx.queryParam("q")
        val notFound = ctx.queryParam("notfound") != null
        if (user != null && !notFound) {
            ctx.redirect("/user/$user")
        } else {
            ctx.renderVelocity("search.vm", model("q", ctx.queryParam("q") ?: ""));
        }
    }

    app.exception(Exception::class.java) { e, ctx -> ctx.status(500) }

}



