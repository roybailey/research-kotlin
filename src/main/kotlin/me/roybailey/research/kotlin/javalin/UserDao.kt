package me.roybailey.research.kotlin.javalin

import java.util.*

data class User(
        val id: String,
        val name: String,
        val email: String)

class UserDao {

    val userList = listOf(
            User(name = "Alice", email = "alice@alice.kt", id = randomId()),
            User(name = "Bob", email = "bob@bob.kt", id = randomId()),
            User(name = "Carol", email = "carol@carol.kt", id = randomId()),
            User(name = "Dave", email = "dave@dave.kt", id = randomId())
    )

    val users = mutableMapOf(userList.map { Pair(it.id, it) }.toMu)

    private fun randomId() = UUID.randomUUID().toString()

    fun save(name: String, email: String) {
        val id = randomId()
        users.put(id, User(name = name, email = email, id = id))
    }

    fun findById(id: Int): User? {
        return users[id]
    }

    fun findByEmail(email: String): User? {
        return users.values.find { it.email == email }
    }

    fun update(id: Int, user: User) {
        users.put(id, User(name = user.name, email = user.email, id = id))
    }

    fun delete(id: Int) {
        users.remove(id)
    }

}