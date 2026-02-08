package app

import io.javalin.Javalin
import io.javalin.compression.CompressionStrategy
import io.javalin.http.BadRequestResponse
import io.javalin.http.NotFoundResponse
import io.javalin.http.queryParamAsClass
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.bundled.JavalinVuePlugin
import io.javalin.plugin.bundled.RateLimitPlugin
import io.javalin.vue.VueComponent
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.util.thread.QueuedThreadPool
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

fun main() {

    val log = LoggerFactory.getLogger("app.MainKt")
    Javalin.start { config ->
        config.bundledPlugins.enableSslRedirects()
        config.staticFiles.add("/public", Location.CLASSPATH)
        config.http.compressionStrategy = CompressionStrategy.GZIP
        config.unsafe.jettyInternal.server = Server(QueuedThreadPool(200, 8, 120000)).apply {
            connectors = arrayOf(ServerConnector(server).apply {
                port = Config.getPort() ?: 7070
                idleTimeout = 120_000
                connectionFactories.filterIsInstance<HttpConnectionFactory>().forEach {
                    it.httpConfiguration.sendServerVersion = false
                }
            })
        }
        config.registerPlugin(RateLimitPlugin({}))
        config.registerPlugin(JavalinVuePlugin { vue ->
            vue.optimizeDependencies = false
        })

        // Routes
        config.routes.before("/api/*") { it.with(RateLimitPlugin::class).requestPerTimeUnit(20, TimeUnit.MINUTES) }
        config.routes.get("/api/can-load") { ctx ->
            val user = ctx.queryParamAsClass<String>("user").required().get()
            // Use quick check that doesn't consume GitHub API requests
            // This runs before the spinner is shown
            ctx.status(if (UserService.canLoadUserQuick(user)) 200 else 400)
        }
        config.routes.get("/api/user/{user}") { ctx ->
            val user = ctx.pathParam("user")
            if (!UserService.userExists(user)) throw NotFoundResponse()
            UserService.getUserIfCanLoad(user)?.let { ctx.json(it) } ?: throw BadRequestResponse("Can't load user")
        }
        config.routes.get("/search", VueComponent("search-view"))
        config.routes.get("/user/{user}", VueComponent("user-view"))
        config.routes.ws("/rate-limit-status") { ws ->
            ws.onConnect { GhService.registerClient(it) }
            ws.onClose { GhService.unregisterClient(it) }
            ws.onError { GhService.unregisterClient(it) }
        }
        config.routes.after { it.cookie("gtm-id", Config.getGtmId() ?: "") } // what is this?

        // Exception and error handlers
        config.routes.exception(Exception::class.java) { e, ctx ->
            log.warn("Uncaught exception", e)
            ctx.status(500)
        }
        config.routes.error(404, "html") {
            it.redirect("/search")
        }
    }

    UserService.syncWatchers()

}
