package me.roybailey.research.kotlin.spark

import me.roybailey.research.kotlin.neo4j.Neo4jService
import me.roybailey.research.kotlin.report.CsvReportVisitor
import me.roybailey.research.kotlin.report.ReportContext
import me.roybailey.research.kotlin.report.ReportEvent
import me.roybailey.research.kotlin.report.SimpleReportVisitor
import mu.KotlinLogging
import org.codehaus.jackson.map.ObjectMapper
import spark.Request
import spark.Response
import spark.Spark.*


fun main(args: Array<String>) {
    Neo4jMovieApp().run()
}

class Neo4jMovieApp {

    private val log = KotlinLogging.logger {}

    val mapper = ObjectMapper()

    
    fun run() {
        with(Neo4jService) {
            init()
            runCypher(loadCypher("/cypher/delete-movies.cypher")!!)
            runCypher(loadCypher("/cypher/create-movies.cypher")!!)
        }

        port(7000)
        exception(Exception::class.java) { exception, request, response ->
            exception.printStackTrace()
        }
        staticFileLocation("public")
        notFound("Not Found!")
        before("/**") { req, rsp ->
            println(req.uri())
        }

        path("/movie") {
            before("/*") { q, a -> log.info("Received api call " + q.uri()) }
            get("", ::getMovies)
        }
    }


    fun getMovies(req: Request, rsp: Response): String? = when {
        (req.headers("Accept").startsWith("text")) -> {
            val results = CsvReportVisitor("Unknown")
            Neo4jService.runCypher(results::reportVisit, "match (m:Movie) return m")
            mapper.writeValueAsString(results.toString())
        }
        else -> {
            val results = SimpleReportVisitor("Unknown")
            Neo4jService.runCypher(results::reportVisit, "match (m:Movie) return m")
            mapper.writeValueAsString(results.data)
        }
    }
}
