package me.roybailey.research.kotlin.neo4j

import me.roybailey.research.kotlin.BaseServiceTest
import me.roybailey.research.kotlin.report.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import java.io.FileOutputStream


class Neo4jServiceTest : BaseServiceTest() {

    private val BASEPATH = "./target/"
    private var txtReport: SimpleReportVisitor = SimpleReportVisitor(Neo4jServiceTest::class.java.simpleName)
    private var csvReport: CsvReportVisitor = CsvReportVisitor(Neo4jServiceTest::class.java.simpleName)
    private var pdfReport: PdfReportVisitor = PdfReportVisitor(Neo4jServiceTest::class.java.simpleName)
    private var xlsReport: ExcelReportVisitor = ExcelReportVisitor(Neo4jServiceTest::class.java.simpleName)
    private var pipeline: CompositeReportVisitor = CompositeReportVisitor()


    @BeforeEach
    fun setupVisitors(testInfo:TestInfo) {
        txtReport = SimpleReportVisitor(testInfo.displayName)
        csvReport = CsvReportVisitor(testInfo.displayName)
        pdfReport = PdfReportVisitor(testInfo.displayName,
                FileOutputStream(BASEPATH + testInfo.displayName.replace(' ', '_') + ".pdf"))
        xlsReport = ExcelReportVisitor(testInfo.displayName,
                FileOutputStream(BASEPATH + testInfo.displayName.replace(' ', '_') + ".xls"))
        pipeline = CompositeReportVisitor(
                txtReport::reportVisit,
                csvReport::reportVisit,
                pdfReport::reportVisit,
                xlsReport::reportVisit
        )
    }


    @Test
    fun `Test Neo4j Nodes`(testInfo: TestInfo) {

        banner(testInfo.displayName) {
            neo4jReportRunner.runReport(
                    ReportDefinition(testInfo.displayName, QueryType.NEO4J, """
                    match (m:Movie)-[:ACTED_IN]-(p:Person)
                    return m as Movie, collect(p.name) as Actors
                    order by Movie.title"""),
                    pipeline::reportVisit)
            val txtOutput = txtReport.toString()
            val csvOutput = csvReport.toString()
            println(txtOutput)
            println(csvOutput)
            assertThat(txtOutput.split("\n")[1]).contains("1992", "A Few Good Men")
            assertThat(csvOutput.split("\n")[1]).contains("1992", "A Few Good Men")
        }
    }


    @Test
    fun `Test Neo4j Node Properties`(testInfo: TestInfo) {

        banner(testInfo.displayName) {
            neo4jReportRunner.runReport(
                    ReportDefinition(testInfo.displayName, QueryType.NEO4J, """
                        match (m:Movie)-[:ACTED_IN]-(p:Person)
                        return m {.title, .released}, collect(p {.name}) as Actors
                        order by m.title"""),
                    pipeline::reportVisit)
            val txtOutput = txtReport.toString()
            val csvOutput = csvReport.toString()
            println(txtOutput)
            println(csvOutput)
            assertThat(txtOutput.split("\n")[1]).contains("1992", "A Few Good Men")
            assertThat(csvOutput.split("\n")[1]).contains("1992", "A Few Good Men")
        }
    }


    @Test
    fun `Test Neo4j ResultSet`(testInfo: TestInfo) {

        banner(testInfo.displayName) {
            neo4jReportRunner.runReport(
                    ReportDefinition(testInfo.displayName, QueryType.NEO4J, """
                        match (m:Movie)-[:ACTED_IN]-(p:Person)
                        return m.title, m.released, collect(p.name) as Actors
                        order by m.title"""),
                    pipeline::reportVisit)
            val txtOutput = txtReport.toString()
            val csvOutput = csvReport.toString()
            println(txtOutput)
            println(csvOutput)
            assertThat(txtOutput.split("\n")[1]).contains("1992", "A Few Good Men")
            assertThat(csvOutput.split("\n")[1]).contains("1992", "A Few Good Men")
        }
    }

}

