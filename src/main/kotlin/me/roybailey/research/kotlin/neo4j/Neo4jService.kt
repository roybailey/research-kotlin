package me.roybailey.research.kotlin.neo4j

import apoc.coll.Coll
import apoc.convert.Json
import apoc.create.Create
import apoc.help.Help
import apoc.index.FulltextIndex
import apoc.load.LoadJson
import apoc.load.Xml
import apoc.meta.Meta
import apoc.path.PathExplorer
import apoc.refactor.GraphRefactoring
import mu.KotlinLogging
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.kernel.api.exceptions.KernelException
import org.neo4j.kernel.configuration.BoltConnector
import org.neo4j.kernel.impl.proc.Procedures
import org.neo4j.kernel.internal.GraphDatabaseAPI
import org.neo4j.procedure.Name
import java.io.File
import java.util.*
import org.neo4j.procedure.UserFunction



data class QueryResultContext(val row:Int, val column:Int)

class Neo4jServiceProcedures {

    @UserFunction("custom.data.encrypt")
    fun format(@Name("value") value: String,
               @Name(value = "key", defaultValue = "") key: String): String {
        return String(Base64.getEncoder().encode(value.toByteArray()))
    }
}

object Neo4jService {

    private val log = KotlinLogging.logger {}

    val neo4jDatabaseFolder = File("./target/neo4j").absoluteFile
    val neo4jConfiguration = Neo4jService::class.java.getResource("/neo4j.conf")
    val neo4jProperties = Properties()

    lateinit var graphDb: GraphDatabaseService

    fun init() {

        log.info("Creating Neo4j Embedded Database into: "+ neo4jDatabaseFolder)

        val graphDbBuilder = GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(neo4jDatabaseFolder)
                .loadPropertiesFromURL(neo4jConfiguration)

        neo4jProperties.load(neo4jConfiguration.openStream())

        val boltConnectorPort = neo4jProperties.getProperty("neo4j.bolt.connector.port", "")
        if(!boltConnectorPort.isEmpty()) {
            val bolt = BoltConnector("0")
            graphDbBuilder.setConfig(bolt.type, "BOLT")
                    .setConfig(bolt.enabled, "true")
                    .setConfig(bolt.listen_address, "localhost:"+boltConnectorPort)
            log.info("Creating Bolt Connector on Port: "+boltConnectorPort)
        }

        graphDb = graphDbBuilder.newGraphDatabase()
        log.info("Created Neo4j Embedded Database from: "+ neo4jConfiguration)

        registerProcedures(listOf(
                Help::class.java,
                Coll::class.java,
                apoc.map.Maps::class.java,
                apoc.load.Jdbc::class.java,
                apoc.text.Strings::class.java,
                Json::class.java,
                Create::class.java,
                apoc.date.Date::class.java,
                FulltextIndex::class.java,
                apoc.lock.Lock::class.java,
                LoadJson::class.java,
                Xml::class.java,
                PathExplorer::class.java,
                Meta::class.java,
                GraphRefactoring::class.java,
                Neo4jServiceProcedures::class.java
        ))
        log.info("Registered Neo4j Apoc Procedures")

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
                    procedures.registerFunction(proc)
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


    private fun defaultVisitor(result: QueryResultContext, name: String, value: Any?) {}

    fun runCypher(cypher: String) {
        runCypher(this::defaultVisitor, cypher)
    }

    fun runCypher(visitor: (QueryResultContext, String, Any?) -> Unit, cypher: String) {
        Neo4jService.graphDb.beginTx().use { tx ->
            //result = graphDb.execute(cypher)
            val srs = Neo4jService.graphDb.execute(cypher)
            var row = 0
            while (srs.hasNext()) {
                val record = srs.next()
                var col = 0;
                srs.columns().forEach { name: String ->
                    val value = record.getValue(name)
                    if (value is Node) {
                        value.allProperties.forEach { prop ->
                            visitor(QueryResultContext(row,col), name + "." + prop.key, prop.value)
                        }
                    } else {
                        if(value == null)
                            log.info { "$value is null" }
                        visitor(QueryResultContext(row,col), name, value)
                    }
                    ++col;
                }
                ++row
            }
            tx.success()
        }
    }

}
