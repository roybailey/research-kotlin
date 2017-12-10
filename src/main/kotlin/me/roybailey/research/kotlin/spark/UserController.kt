package me.roybailey.research.kotlin.spark

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import spark.Request
import spark.Response


object UserController {

    val userDao = UserDao()
    val mapper = ObjectMapper().registerModule(KotlinModule())


    fun getUsers(req: Request, rsp: Response):String {
        val users = userDao.users
        val valueAsString = mapper.writeValueAsString(users)
        //rsp.type("application/json")
        return valueAsString
    }

    data class UserTemplate(
            val id: String? = null,
            val name: String? = null,
            val email: String? = null
    )

    fun createUser(req: Request, rsp: Response):String {
        val newUser = mapper.readValue(req.body(),UserTemplate::class.java)
        val savedUser = userDao.save(newUser.name!!, newUser.email!!)
        return mapper.writeValueAsString(savedUser)
    }

    fun getUser(req: Request, rsp: Response):String {
        return mapper.writeValueAsString(userDao.findById(req.params(":user-id")!!)!!)
    }

    fun updateUser(req: Request, rsp: Response):String {
        val updatedUserTemplate = mapper.readValue(req.body(),UserTemplate::class.java)
        val updatedUser = User(
                id = updatedUserTemplate.id!!,
                name = updatedUserTemplate.name!!,
                email = updatedUserTemplate.email!!
        )
        return mapper.writeValueAsString(userDao.update(updatedUser.id, updatedUser))
    }

    fun deleteUser(req: Request, rsp: Response):String? {
        userDao.delete(req.params(":user-id")!!)
        return req.params(":user-id")
    }

}
