package me.roybailey.research.kotlin.neo4j


fun main(args: Array<String>) {

    Neo4jService.init()

    val movies = Neo4jService.loadCypher("/cypher/create-movies.cypher")

    /* loads a ton of transactions to flood the tx logs for testing the settings */
    for (idx in 1..5000000) {
        Neo4jService.runCypher(movies!!)
        if(idx % 1000 == 0)
            println("loaded...$idx times")
    }
}

