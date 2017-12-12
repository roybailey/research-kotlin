package me.roybailey.research.kotlin.neo4j

import me.roybailey.research.kotlin.BaseServiceTest
import me.roybailey.research.kotlin.report.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.io.FileOutputStream


class Neo4jServiceTest : BaseServiceTest() {

    private val BASEPATH = "./target/"
    private var txtReport: SimpleReportVisitor = SimpleReportVisitor(Neo4jServiceTest::class.java.simpleName)
    private var csvReport: CsvReportVisitor = CsvReportVisitor(Neo4jServiceTest::class.java.simpleName)
    private var pdfReport: PdfReportVisitor = PdfReportVisitor(Neo4jServiceTest::class.java.simpleName)
    private var xlsReport: ExcelReportVisitor = ExcelReportVisitor(Neo4jServiceTest::class.java.simpleName)
    private var pipeline: CompositeReportVisitor = CompositeReportVisitor()


    @Before
    fun setupVisitors() {
        txtReport = SimpleReportVisitor(testName.methodName)
        csvReport = CsvReportVisitor(testName.methodName)
        pdfReport = PdfReportVisitor(testName.methodName,
                FileOutputStream(BASEPATH+testName.methodName.replace(' ','_')+".pdf"))
        xlsReport = ExcelReportVisitor(testName.methodName,
                FileOutputStream(BASEPATH+testName.methodName.replace(' ','_')+".xls"))
        pipeline = CompositeReportVisitor(
                txtReport::reportVisit,
                csvReport::reportVisit,
                pdfReport::reportVisit,
                xlsReport::reportVisit
        )
    }


    @Test
    fun `Test Neo4j Nodes`() {

        with(neo4j) {
            banner(testName.methodName) {
                runCypher("""
                        match (m:Movie)-[:ACTED_IN]-(p:Person)
                        return m as Movie, collect(p.name) as Actors
                        order by Movie.title""",
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


    @Test
    fun `Test Neo4j Node Properties`() {

        with(neo4j) {
            banner(testName.methodName) {
                runCypher("""
                        match (m:Movie)-[:ACTED_IN]-(p:Person)
                        return m {.title, .released}, collect(p {.name}) as Actors
                        order by m.title""",
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


    @Test
    fun `Test Neo4j ResultSet`() {

        with(neo4j) {
            banner(testName.methodName) {
                runCypher("""
                        match (m:Movie)-[:ACTED_IN]-(p:Person)
                        return m.title, m.released, collect(p.name) as Actors
                        order by m.title""",
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

}

