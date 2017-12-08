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
import org.neo4j.graphdb.*
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.kernel.api.exceptions.KernelException
import org.neo4j.kernel.configuration.BoltConnector
import org.neo4j.kernel.impl.proc.Procedures
import org.neo4j.kernel.internal.GraphDatabaseAPI
import java.io.File
import java.io.PrintWriter
import java.lang.Exception


object Neo4jService {

    var neo4jDatabaseFolder = File("./target/neo4j").absoluteFile
    var neo4jConfiguration = Neo4jService::class.java.getResource("/neo4j.conf")

    lateinit var graphDb: GraphDatabaseService

    fun init() {
        val bolt = BoltConnector("0")

        graphDb = GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder(neo4jDatabaseFolder)
                .loadPropertiesFromURL(neo4jConfiguration)
                .setConfig( bolt.type, "BOLT" )
                .setConfig( bolt.enabled, "true" )
                .setConfig( bolt.listen_address, "localhost:7887" )
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

    fun runCypher(cypher: String) : Result {
        return runCypher(null, cypher)
    }

    fun runCypher(visitor : Result.ResultVisitor<Exception>?, cypher: String) : Result {
        var result : Result = EmptyResult()
        graphDb.beginTx().use { tx ->
            result = graphDb.execute(cypher)
            tx.success()
        }
        return result
    }

}

class EmptyResult : Result {
    override fun remove() {
    }

    override fun getQueryExecutionType(): QueryExecutionType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun writeAsStringTo(p0: PrintWriter?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <T : Any?> columnAs(p0: String?): ResourceIterator<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun next(): MutableMap<String, Any> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun columns(): MutableList<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun <VisitationException : Exception?> accept(p0: Result.ResultVisitor<VisitationException>?) {
        return
    }

    override fun getExecutionPlanDescription(): ExecutionPlanDescription {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resultAsString(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hasNext(): Boolean {
        return false
    }

    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getQueryStatistics(): QueryStatistics {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNotifications(): MutableIterable<Notification> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}