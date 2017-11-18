package me.roybailey.research.kotlin.javalin

import io.javalin.Context
import me.roybailey.research.kotlin.javalin.User


object UserController {

    val userDao = UserDao()


    fun getUsers(ctx: Context) {
        ctx.json(userDao.users)
    }

    data class UserTemplate(
            val id: String? = null,
            val name: String? = null,
            val email: String? = null
    )

    fun createUser(ctx: Context) {
        val newUser = ctx.bodyAsClass(UserTemplate::class.java)
        userDao.save(newUser.name!!, newUser.email!!)
    }

    fun getUser(ctx: Context) {
        ctx.json(userDao.findById(ctx.param(":user-id")!!)!!)
    }

    fun updateUser(ctx: Context) {
        val updatedUserTemplate = ctx.bodyAsClass(UserTemplate::class.java)
        val updatedUser = User(
                id = updatedUserTemplate.id!!,
                name = updatedUserTemplate.name!!,
                email = updatedUserTemplate.email!!
        )
        userDao.update(updatedUser.id, updatedUser)
    }

    fun deleteUser(ctx: Context) {
        userDao.delete(ctx.param(":user-id")!!)
    }

}
