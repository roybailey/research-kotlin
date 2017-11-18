package me.roybailey.research.kotlin.javalin

import java.util.*

data class User(
        val id: String,
        val name: String,
        val email: String)

class UserDao {

    private val userList = listOf(
            User(name = "Alice", email = "alice@alice.kt", id = randomId()),
            User(name = "Bob", email = "bob@bob.kt", id = randomId()),
            User(name = "Carol", email = "carol@carol.kt", id = randomId()),
            User(name = "Dave", email = "dave@dave.kt", id = randomId())
    )

    val users = mutableMapOf(*userList.map { Pair(it.id, it) }.toTypedArray())

    private fun randomId() = UUID.randomUUID().toString()

    fun save(name: String, email: String): User? {
        val id = randomId()
        users.put(id, User(name = name, email = email, id = id))
        return findById(id)
    }

    fun save(user: User): User? = save(user.name, user.email)

    fun findById(id: String): User? = users[id]

    fun findByEmail(email: String): User? = users.values.find { it.email == email }

    fun update(id: String, user: User): User? {
        users.put(id, User(name = user.name, email = user.email, id = id))
        return findById(id)
    }

    fun delete(id: String) {
        users.remove(id)
    }

}