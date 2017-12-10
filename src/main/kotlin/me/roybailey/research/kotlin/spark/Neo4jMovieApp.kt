package me.roybailey.research.kotlin.spark

import me.roybailey.research.kotlin.neo4j.Neo4jService
import me.roybailey.research.kotlin.neo4j.QueryResultContext
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

    fun getMovies(req: Request, rsp: Response): String? {
        // first run the query and capture the results...
        val results = mutableListOf<MutableMap<String, Any>>()
        val captureResults = fun(ctx: QueryResultContext, name: String, value: Any?) {
            printResults(results, ctx, name, value)
        }
        Neo4jService.runCypher(captureResults, "match (m:Movie) return m")
        // second return an appropriate representation of the data based on requested format...
        return if(req.headers("Accept").startsWith("text")) {
            var csv = results[0].keys.joinToString(",")
            results.forEach { record ->
                csv += "\n"
                csv += record.values.joinToString(",")
            }
            return csv
        }
        else mapper.writeValueAsString(results)
    }

    fun printResults(capture: MutableList<MutableMap<String, Any>>, ctx: QueryResultContext, name: String, value: Any?) {
        if (capture.size == ctx.row) {
            if (ctx.row > 0) println()
            capture.add(mutableMapOf())
        }
        print("( name=$name value=$value )")
        capture.last().put(name, value!!)
    }
}
