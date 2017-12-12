package app.util

import io.javalin.Javalin

object Heroku {

    fun getPort() = ProcessBuilder().environment()["PORT"]?.let {
        Integer.parseInt(it)
    }

    fun getOauthToken() = ProcessBuilder().environment()["OAUTH_TOKEN"]

    fun enableSslRedirect(app: Javalin) {
        // This really should be solved in Heroku itself, but
        // I'm incapable of finding any setting for it
        app.before { ctx ->
            if (ctx.header("x-forwarded-proto") == "http") {
                ctx.redirect("https://${ctx.header("host")}${ctx.path()}")
            }
        }
    }

}
