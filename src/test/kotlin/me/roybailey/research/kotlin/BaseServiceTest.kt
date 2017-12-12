package me.roybailey.research.kotlin

import me.roybailey.research.kotlin.neo4j.Neo4jService
import me.roybailey.research.kotlin.report.ReportService
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.TestName


open class BaseServiceTest {

    companion object {

        lateinit var neo4j: Neo4jService
        lateinit var reportService: ReportService

        fun banner(message: String, body: () -> Unit) {
            println("########## $message ##########")
            body()
            println()
        }

        @JvmStatic
        @BeforeClass
        fun setupDatabase() {
            banner("SetupDatabase") {
                neo4j = Neo4jService()
                reportService = ReportService(neo4j)

                with(neo4j) {
                    runCypher("ClearDatabase", loadCypher("/cypher/delete-movies.cypher")!!)
                    runCypher("LoadMatrix", loadCypher("/cypher/create-movies.cypher")!!)
                }
            }
        }

        @JvmStatic
        @AfterClass
        fun shutdownDatabase() {
            banner("ShutdownDatabase") {
                with(neo4j) {
                    try {
                        shutdown()
                    } catch (ignore:Exception) {}
                }
            }
        }
    }


    @Rule
    @JvmField
    val testName = TestName()

}