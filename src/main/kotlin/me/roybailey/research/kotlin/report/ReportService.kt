package me.roybailey.research.kotlin.report

import com.google.common.base.Stopwatch
import com.google.common.net.MediaType
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import me.roybailey.research.kotlin.neo4j.Neo4jService
import mu.KotlinLogging
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit


enum class QueryType {
    NEO4J,
    JDBC,
    API
}

data class ReportColumn(
        val name: String,
        val type: String = String::class.java.simpleName,
        val width: Int = 4,
        val format: String = ""
)

data class ReportDefinition(
        val reportName: String,
        val queryType: QueryType,
        val query: String,
        val columns: List<ReportColumn> = emptyList()
)

data class ReportOutput(
        val contentType: String = MediaType.CSV_UTF_8.toString(),
        val outputName: String,
        val outputStream: OutputStream = ByteOutputStream()
)


class ReportService(val neo4j: Neo4jService) {

    private val log = KotlinLogging.logger {}

    fun runReport(report: ReportDefinition, output: List<ReportOutput>) {
        val stopwatch = Stopwatch.createStarted()
        log.info("Running Report [${report.reportName}]")
        val visitors = CompositeReportVisitor()
        output.forEach {
            when(it.contentType) {
                MediaType.CSV_UTF_8.toString() -> {
                    val csvVisitor = CsvReportVisitor(it.outputName, writer = OutputStreamWriter(it.outputStream))
                    visitors.visitors += csvVisitor::reportVisit
                    log.info("Created CSV visitor [${it.outputName}]")
                }

            }
        }

        log.info("Running Report Query Type ${report.queryType}")
        log.debug("Running Report Query \n${report.query}\n")
        when(report.queryType) {
            QueryType.NEO4J -> neo4j.runCypher(report.query, visitors::reportVisit)
        }

        log.info("Completed Report [${report.reportName}] in ${stopwatch.elapsed(TimeUnit.SECONDS)} seconds")
    }
}
