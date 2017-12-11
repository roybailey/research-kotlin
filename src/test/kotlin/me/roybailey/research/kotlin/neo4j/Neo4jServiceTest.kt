package me.roybailey.research.kotlin.neo4j

import me.roybailey.research.kotlin.report.CompositeReportVisitor
import me.roybailey.research.kotlin.report.CsvReportVisitor
import me.roybailey.research.kotlin.report.SimpleReportVisitor
import org.assertj.core.api.Assertions.assertThat
import org.junit.*
import org.junit.rules.TestName


class Neo4jServiceTest {

    companion object {
        fun banner(message: String, body: () -> Unit) {
            println("########## $message ##########")
            body()
            println()
        }

        @JvmStatic
        @BeforeClass
        fun setupDatabase() {
            banner("SetupDatabase") {
                with(Neo4jService) {
                    init()
                    runCypher("ClearDatabase", loadCypher("/cypher/delete-movies.cypher")!!)
                    runCypher("LoadMatrix", Neo4jService.loadCypher("/cypher/create-movies.cypher")!!)
                }
            }
        }
    }


    @Rule
    @JvmField
    val testName = TestName()


    private var txtReport: SimpleReportVisitor = SimpleReportVisitor(Neo4jServiceTest::class.java.simpleName)
    private var csvReport: CsvReportVisitor = CsvReportVisitor(Neo4jServiceTest::class.java.simpleName)
    private var pipeline: CompositeReportVisitor = CompositeReportVisitor()


    @Before
    fun setupVisitors() {
        txtReport = SimpleReportVisitor(testName.methodName)
        csvReport = CsvReportVisitor(testName.methodName)
        pipeline = CompositeReportVisitor(txtReport::reportVisit, csvReport::reportVisit)
    }


    @Test
    fun `Test Neo4j Nodes`() {

        with(Neo4jService) {
            banner(testName.methodName) {
                runCypher(pipeline::reportVisit,
                        "match (m:Movie)-[:ACTED_IN]-(p:Person) return m as Movie, collect(p.name) as Actors order by Movie.title")
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

        with(Neo4jService) {
            banner(testName.methodName) {
                runCypher(pipeline::reportVisit,
                        "match (m:Movie)-[:ACTED_IN]-(p:Person) return m {.title, .released}, collect(p {.name}) as Actors order by m.title")
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

        with(Neo4jService) {
            banner(testName.methodName) {
                runCypher(pipeline::reportVisit,
                        "match (m:Movie)-[:ACTED_IN]-(p:Person) return m.title, m.released, collect(p.name) as Actors order by m.title")
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
