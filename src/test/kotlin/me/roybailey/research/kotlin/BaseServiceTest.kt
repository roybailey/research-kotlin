package me.roybailey.research.kotlin

import me.roybailey.research.kotlin.neo4j.Neo4jService
import me.roybailey.research.kotlin.report.ReportService
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.TestName


open class BaseServiceTest {

    companion object {

        val neo4j = Neo4jService()
        val reportService = ReportService(neo4j)

        fun banner(message: String, body: () -> Unit) {
            println("########## $message ##########")
            body()
            println()
        }

        @JvmStatic
        @BeforeClass
        fun setupDatabase() {
            banner("SetupDatabase") {
                with(neo4j) {
                    runCypher("ClearDatabase", loadCypher("/cypher/delete-movies.cypher")!!)
                    runCypher("LoadMatrix", loadCypher("/cypher/create-movies.cypher")!!)
                }
            }
        }
    }


    @Rule
    @JvmField
    val testName = TestName()

}