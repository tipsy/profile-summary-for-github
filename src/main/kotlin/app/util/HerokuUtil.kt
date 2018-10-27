package app.util

import io.javalin.Javalin

object HerokuUtil {

    /**
     * Force HTTPS for all requests
     *
     * This really should be solved in Heroku itself,
     * but I'm incapable of finding any setting for it
     */
    fun enableSslRedirect(app: Javalin) {
        app.before { ctx ->
            if (ctx.header("x-forwarded-proto") == "http") {
                val queryString = ctx.queryString()?.let { query -> "?$query" } ?: ""
                ctx.redirect("https://${ctx.header("host")}${ctx.path()}$queryString", 301)
            }
        }
    }

}
