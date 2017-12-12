package me.roybailey.research.kotlin.neo4j


fun main(args: Array<String>) {

    val neo4j = Neo4jService()

    val movies = neo4j.loadCypher("/cypher/create-movies.cypher")

    /* loads a ton of transactions to flood the tx logs for testing the settings */
    for (idx in 1..5000000) {
        neo4j.execute(movies!!) {
            if(idx % 1000 == 0)
                println("loaded...$idx times")
        }
    }
}

