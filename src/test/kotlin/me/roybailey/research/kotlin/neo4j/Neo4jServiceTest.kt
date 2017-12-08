package me.roybailey.research.kotlin.neo4j

import org.junit.Test
import org.neo4j.graphdb.Node
import java.util.*


class Neo4jServiceTest {

    @Test
    fun testNeo4jService() {

        Neo4jService.init()

        Neo4jService.runCypher(Neo4jService.loadCypher("/cypher/delete-movies.cypher")!!)
        println("Running  creation cypher..."+ Date())
        Neo4jService.runCypher(Neo4jService.loadCypher("/cypher/create-movies.cypher")!!)
        println("Finished creation cypher..."+ Date())

        Neo4jService.graphDb.beginTx().use { tx ->
            //result = graphDb.execute(cypher)
            val srs = Neo4jService.graphDb.execute(
                    "match (m:Movie) return m")
            while(srs.hasNext()) {
                val record = srs.next()
                srs.columns().forEach { name:String ->
                    val value = record.getValue(name)
                    if(value is Node) {
                        value.allProperties.forEach{ prop ->
                            print(" ${value.labels}.${prop.key}=${prop.value}")
                        }
                    } else {
                        print(" $name=${record.getValue(name)}")
                    }
                }
                println()
            }
            tx.success()
        }

    }
}
