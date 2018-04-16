package me.roybailey.research.kotlin.spark

import spark.Spark.*
import spark.Spark.exception




fun main(args: Array<String>) {

    port(7001)
    exception(Exception::class.java) { exception, request, response ->
        exception.printStackTrace()
    }
    staticFileLocation("/public")
    notFound("Not Found!")
    before("/**") { req, rsp ->
        println(req.uri())
    }

    path("/users") {
        get("", UserController::getUsers)
        post("", UserController::createUser)
        get("/:user-id", UserController::getUser)
        patch("/:user-id", UserController::updateUser)
        delete("/:user-id", UserController::deleteUser)
    }
}

