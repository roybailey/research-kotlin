package me.roybailey.research.kotlin.javalin

import io.javalin.Context
import me.roybailey.research.kotlin.javalin.User


object UserController {

    val userDao = UserDao()


    fun getUsers(ctx: Context) {
        ctx.json(userDao.users)
    }

    fun createUser(ctx: Context) {
        val newUser = ctx.bodyAsClass(User::class.java)
        userDao.save(newUser.name, newUser.email)
    }

    fun getUser(ctx: Context) {
        ctx.json(userDao.getUser(ctx.param(":user-id")!!))
    }

    fun updateUser(ctx: Context) {
        val updatedUser = ctx.bodyAsClass(User::class.java)
        userDao.save(updatedUser)
    }

    fun deleteUser(ctx: Context) {
        userDao.delete(ctx.param(":user-id")!!)
    }

}
