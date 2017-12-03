package me.roybailey.research.kotlin.javalin

import io.javalin.ApiBuilder.*
import io.javalin.Context
import io.javalin.Javalin
import me.roybailey.research.kotlin.neo4j.Neo4jService
import org.neo4j.graphdb.Result


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
    var result = Neo4jService.runCypher("match (m:Movie) return m.title as title")
    result.accept<Exception>({ row ->
        println(row); true
    })
    ctx.json(result)
}
