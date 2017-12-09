package me.roybailey.research.kotlin.neo4j

import org.junit.Test
import java.util.*


class Neo4jServiceTest {

    @Test
    fun testNeo4jService() {

        with(me.roybailey.research.kotlin.neo4j.Neo4jService) {
            init()
            runCypher(loadCypher("/cypher/delete-movies.cypher")!!)
            println("Running  creation cypher..." + Date())
            runCypher(Neo4jService.loadCypher("/cypher/create-movies.cypher")!!)
            println("Finished creation cypher..." + Date())

            println("########## movie nodes ##########")
            runCypher(::printResults, "match (m:Movie) return m")
            println()
            println("########## movie node properties ##########")
            runCypher(::printResults, "match (m:Movie) return m {.title, .released}")
            println()
            println("########## movie properties ##########")
            runCypher(::printResults, "match (m:Movie) return m.title, m.released")
            println()
        }
    }

}

fun printResults(row: Int, name: String, value: Any) {
    print(" (name=$name value=$value)")
}
