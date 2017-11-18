package me.roybailey.research.kotlin.javalin

import io.javalin.ApiBuilder.*
import io.javalin.Javalin


fun main(args: Array<String>) {

    val app = Javalin.create().apply {
        exception(Exception::class.java) { e, ctx -> e.printStackTrace() }
        enableStaticFiles("/public")
        enableStandardRequestLogging()
        error(404) { ctx -> ctx.json("not found") }
        port(7000)
    }.start()

    app.routes {
        path("users") {
            get(UserController::getUsers)
            post(UserController::createUser)
            path(":user-id") {
                get(UserController::getUser)
                patch(UserController::updateUser)
                delete(UserController::deleteUser)
            }
        }
    }
}

