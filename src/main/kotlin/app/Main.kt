package app

import io.javalin.Javalin
import io.javalin.translator.template.TemplateUtil.model

fun main(args: Array<String>) {

    val app = Javalin.create().apply {
        port(7070)
        enableStaticFiles("/public")
        enableDynamicGzip()
    }.start()

    app.get("/user/:user-id") { ctx ->
        ctx.renderVelocity("user.vm", model("user-id", ctx.param("user-id")!!))
    }

    app.get("/api/user/:user-id") { ctx ->
        ctx.json(UserController.getUserProfile(ctx.param("user-id")!!))
    }

    app.exception(Exception::class.java) { e, ctx ->
        when {
            e.message == "Not Found (404)" -> ctx.status(404)
            else -> ctx.status(500)
        }
    }

}



