package app.util

import io.javalin.Javalin

object Heroku {

    // Get port from Heroku, or return null (localhost)
    fun getPort() = ProcessBuilder().environment()["PORT"]?.let {
        Integer.parseInt(it)
    }

    // Get oauth-token from Heroku, or return null (localhost)
    fun getApiTokens() = ProcessBuilder().environment()["API_TOKENS"] ?: System.getProperty("api-tokens")

    // Force HTTPS for all requests
    fun enableSslRedirect(app: Javalin) {
        // This really should be solved in Heroku itself, but I'm incapable of finding any setting for it
        app.before { ctx ->
            if (ctx.header("x-forwarded-proto") == "http") {
                ctx.redirect("https://${ctx.header("host")}${ctx.path()}", 301)
            }
        }
    }

}
