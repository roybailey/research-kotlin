package me.roybailey.research.kotlin.neo4j

import apoc.coll.Coll
import apoc.convert.Json
import apoc.create.Create
import apoc.help.*
import apoc.index.FulltextIndex
import apoc.load.LoadJson
import apoc.load.Xml
import apoc.meta.Meta
import apoc.path.PathExplorer
import apoc.refactor.GraphRefactoring
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.kernel.api.exceptions.KernelException
import org.neo4j.kernel.impl.proc.Procedures
import org.neo4j.kernel.internal.GraphDatabaseAPI
import java.io.File


object Neo4jService {

    var neo4jDatabaseFolder = File("/Users/roybailey/Coding/github/research-kotlin/neo4j")
    var neo4jConfiguration = Neo4jService::class.java.getResource("/neo4j.conf")

    lateinit var graphDb: GraphDatabaseService

    fun init() {

        graphDb = GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(neo4jDatabaseFolder)
                .loadPropertiesFromURL(neo4jConfiguration)
                .newGraphDatabase()

        registerProcedures(listOf(
                Help::class.java,
                Coll::class.java,
                apoc.map.Maps::class.java,
                Json::class.java,
                Create::class.java,
                apoc.date.Date::class.java,
                FulltextIndex::class.java,
                apoc.lock.Lock::class.java,
                LoadJson::class.java,
                Xml::class.java,
                PathExplorer::class.java,
                Meta::class.java,
                GraphRefactoring::class.java
        ))

        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                graphDb.shutdown()
            }
        })
    }

    fun isEmbedded(): Boolean {
        return true
    }


    fun registerProcedures(toRegister: List<Class<*>>) {
        if (isEmbedded()) {
            val procedures = (graphDb as GraphDatabaseAPI).dependencyResolver.resolveDependency(Procedures::class.java)
            toRegister.forEach { proc ->
                try {
                    procedures.registerProcedure(proc)
                } catch (e: KernelException) {
                    throw RuntimeException("Error registering " + proc, e)
                }
            }
        }
    }


    fun loadCypher(filename: String): String? {
        val cypher = Neo4jService::class.java.getResource(filename).readText()
        return cypher
    }


    fun runCypher(cypher: String) {
        graphDb.beginTx().use { tx ->
            graphDb.execute(cypher)
            tx.success()
        }
    }

}