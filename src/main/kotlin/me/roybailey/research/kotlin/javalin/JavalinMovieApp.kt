package me.roybailey.research.kotlin.javalin

import io.javalin.ApiBuilder.get
import io.javalin.ApiBuilder.path
import io.javalin.Context
import io.javalin.Javalin
import me.roybailey.research.kotlin.neo4j.Neo4jService


fun main(args: Array<String>) {

    Neo4jService.init()

    Neo4jService.runCypher(Neo4jService.loadCypher("/cypher/create-movies.cypher")!!)

    val app = Javalin.create().apply {
        exception(Exception::class.java) { e, ctx -> e.printStackTrace() }
        enableStaticFiles("/public")
        enableStandardRequestLogging()
        error(404) { ctx -> ctx.json("not found") }
        port(7000)
    }.start()

    app.routes {
        path("movie") {
            get(::getMovies)
        }
    }
}

fun getMovies(ctx: Context) {
    // first run the query and capture the results...
    val results = mutableListOf<MutableMap<String, Any>>()
    val captureResults = fun(row: Int, name: String, value: Any) {
        printResults(results, row, name, value)
    }
    Neo4jService.runCypher(captureResults, "match (m:Movie) return m")
    // second return an appropriate representation of the data based on requested format...
    ctx.json(results)
}

fun printResults(capture: MutableList<MutableMap<String, Any>>, row: Int, name: String, value: Any) {
    if (capture.size == row) {
        if (row > 0) println()
        capture.add(mutableMapOf())
    }
    print("( name=$name value=$value )")
    capture.last().put(name, value)
}
