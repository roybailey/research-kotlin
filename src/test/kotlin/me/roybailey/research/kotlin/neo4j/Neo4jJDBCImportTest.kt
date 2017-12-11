package me.roybailey.research.kotlin.neo4j

import me.roybailey.research.kotlin.report.SimpleReportVisitor
import mu.KotlinLogging
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import java.io.File


class Neo4jJDBCImportTest {

    private val log = KotlinLogging.logger {}

    @Rule @JvmField
    val testName = TestName()

    @Test
    fun `Test Neo4j JDBC Load`() {

        with(me.roybailey.research.kotlin.neo4j.Neo4jService) {
            init()

            val csvTestData = File("./src/test/resources/testdata/").absolutePath + "/SampleCSVFile_53000kb.csv"
            val selectQuery = "SELECT * FROM CSVREAD('$csvTestData')"
            val cypherQuery = """CALL apoc.load.jdbc('jdbc:h2:mem:test;DB_CLOSE_DELAY=-1',"$selectQuery") YIELD row
                RETURN
                row.PRODUCT as PRODUCT,
                custom.data.encrypt(row.FULLNAME) as FULLNAME,
                row.PRICE as PRICE,
                row.UNITPRICE as UNITPRICE,
                apoc.text.toUpperCase(COALESCE(row.CATEGORY, "")) as CATEGORY,
                row.BRAND as BRAND,
                row.QUANTITY as QUANTITY,
                row.DISCOUNT as DISCOUNT
            """
            val results = SimpleReportVisitor(testName.methodName)
            runCypher(cypherQuery, results::reportVisit)
            println(results)
        }
    }
}
