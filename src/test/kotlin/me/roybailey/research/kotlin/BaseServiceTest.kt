package me.roybailey.research.kotlin

import me.roybailey.research.kotlin.neo4j.Neo4jReportRunner
import me.roybailey.research.kotlin.neo4j.Neo4jService
import me.roybailey.research.kotlin.report.ReportRunner
import me.roybailey.research.kotlin.report.ReportService
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll


open class BaseServiceTest {

    companion object {

        lateinit var neo4j: Neo4jService
        lateinit var neo4jReportRunner: ReportRunner
        lateinit var reportService: ReportService

        fun banner(message: String, body: () -> Unit) {
            println("########## $message ##########")
            body()
            println()
        }

        @JvmStatic
        @BeforeAll
        fun setupDatabase() {
            banner("SetupDatabase") {
                neo4j = Neo4jService()
                neo4jReportRunner = Neo4jReportRunner(neo4j)
                reportService = ReportService(neo4j)

                with(neo4j) {
                    execute(loadCypher("/cypher/delete-movies.cypher")!!) {}
                    execute(loadCypher("/cypher/create-movies.cypher")!!) {}
                }
            }
        }

        @JvmStatic
        @AfterAll
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


}
