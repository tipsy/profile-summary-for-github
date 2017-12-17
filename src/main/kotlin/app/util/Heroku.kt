package app.util

import io.javalin.Javalin

object Heroku {

    // Get port from Heroku, or return null (localhost)
    fun getPort() = ProcessBuilder().environment()["PORT"]?.let {
        Integer.parseInt(it)
    }

    // Get 'api-tokens' from Heroku/System, or return null if not set
    fun getApiTokens() = ProcessBuilder().environment()["API_TOKENS"] ?: System.getProperty("api-tokens")

    // Get 'unrestricted' state from Heroku/System, or return null if not set
    fun getUnrestrictedState() = ProcessBuilder().environment()["UNRESTRICTED"] ?: System.getProperty("unrestricted")

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
