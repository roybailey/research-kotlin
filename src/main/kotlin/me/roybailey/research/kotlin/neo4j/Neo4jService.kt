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
import me.roybailey.research.kotlin.report.ReportContext
import me.roybailey.research.kotlin.report.ReportEvent
import me.roybailey.research.kotlin.report.SimpleReportVisitor
import mu.KotlinLogging
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Result
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.kernel.api.exceptions.KernelException
import org.neo4j.kernel.configuration.BoltConnector
import org.neo4j.kernel.impl.proc.Procedures
import org.neo4j.kernel.internal.GraphDatabaseAPI
import org.neo4j.procedure.Name
import org.neo4j.procedure.UserFunction
import java.io.File
import java.util.*


class Neo4jServiceProcedures {

    @UserFunction("custom.data.encrypt")
    fun format(@Name("value") value: String,
               @Name(value = "key", defaultValue = "") key: String): String =
            String(Base64.getEncoder().encode(value.toByteArray()))
}


object Neo4jService {

    private val log = KotlinLogging.logger {}

    val neo4jDatabaseFolder = File("./target/neo4j").absoluteFile
    val neo4jConfiguration = Neo4jService::class.java.getResource("/neo4j.conf")
    val neo4jProperties = Properties()

    lateinit var graphDb: GraphDatabaseService


    fun init() {

        log.info("Creating Neo4j Embedded Database into: " + neo4jDatabaseFolder)

        val graphDbBuilder = GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(neo4jDatabaseFolder)
                .loadPropertiesFromURL(neo4jConfiguration)

        neo4jProperties.load(neo4jConfiguration.openStream())

        val boltConnectorPort = neo4jProperties.getProperty("neo4j.bolt.connector.port", "")

        if (!boltConnectorPort.isEmpty()) {
            val bolt = BoltConnector("0")
            graphDbBuilder.setConfig(bolt.type, "BOLT")
                    .setConfig(bolt.enabled, "true")
                    .setConfig(bolt.listen_address, "localhost:" + boltConnectorPort)
            log.info("Creating Bolt Connector on Port: " + boltConnectorPort)
        }

        graphDb = graphDbBuilder.newGraphDatabase()
        log.info("Created Neo4j Embedded Database from: " + neo4jConfiguration)

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


    fun isEmbedded(): Boolean = true


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


    fun execute(cypher: String, code: (result: Result) -> Unit) {
        graphDb.beginTx().use { tx ->
            code(graphDb.execute(cypher))
            tx.success()
        }
    }


    fun runCypher(reportName: String, cypher: String): SimpleReportVisitor {
        val visitor = SimpleReportVisitor(reportName)
        runCypher(cypher) { ctx ->
            visitor.reportVisit(ctx)
        }
        return visitor
    }


    fun runCypher(cypher: String, visitor: (ctx: ReportContext) -> Unit) {
        visitor(ReportContext(ReportEvent.START_REPORT))
        execute(cypher) { srs ->
            var row = 0
            while (srs.hasNext()) {
                visitor(ReportContext(ReportEvent.START_ROW, row = row))
                val record = srs.next()
                var rcol = 0
                srs.columns().forEachIndexed { col, name ->
                    val value = record.getValue(name)
                    if (value is Node) {
                        value.allProperties.forEach { prop ->
                            visitor(ReportContext(ReportEvent.DATA, name + "." + prop.key, prop.value, row, rcol++))
                        }
                    } else if (value is Map<*, *>) {
                        value.keys.forEach { prop ->
                            visitor(ReportContext(ReportEvent.DATA, name + "." + prop, value[prop], row, rcol++))
                        }
                    } else {
                        visitor(ReportContext(ReportEvent.DATA, name, value, row, rcol++))
                    }
                }
                visitor(ReportContext(ReportEvent.END_ROW, row = row))
                ++row
            }
            visitor(ReportContext(ReportEvent.END_REPORT, row = row))
        }
    }

}
