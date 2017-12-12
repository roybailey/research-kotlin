package me.roybailey.research.kotlin.spark

import me.roybailey.research.kotlin.neo4j.Neo4jService
import me.roybailey.research.kotlin.report.CsvReportVisitor
import me.roybailey.research.kotlin.report.SimpleReportVisitor
import mu.KotlinLogging
import org.codehaus.jackson.map.ObjectMapper
import org.eclipse.jetty.http.HttpHeader
import spark.Request
import spark.Response
import spark.Spark.*


fun main(args: Array<String>) {
    Neo4jMovieApp().run()
}

class Neo4jMovieApp {

    private val log = KotlinLogging.logger {}

    val neo4j = Neo4jService()
    val mapper = ObjectMapper()


    fun run() {
        with(neo4j) {
            execute(loadCypher("/cypher/delete-movies.cypher")!!) {}
            execute(loadCypher("/cypher/create-movies.cypher")!!) {}
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
        (req.headers(HttpHeader.ACCEPT.asString()).startsWith("text")) -> {
            rsp.header(HttpHeader.CONTENT_TYPE.asString(), "text/csv")
            val results = CsvReportVisitor("Unknown")
            //neo4j.runCypher("match (m:Movie) return m", results::reportVisit)
            results.toString()
        }
        else -> {
            rsp.header(HttpHeader.CONTENT_TYPE.asString(), "application/json")
            val results = SimpleReportVisitor("Unknown")
            //neo4j.runCypher("match (m:Movie) return m", results::reportVisit)
            mapper.writeValueAsString(results.data)
        }
    }
}
