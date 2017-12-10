package me.roybailey.research.kotlin.neo4j

import mu.KotlinLogging
import org.junit.Test
import java.io.File
import java.lang.String.valueOf


class Neo4jJDBCImportTest {

    private val log = KotlinLogging.logger {}


    @Test
    fun `Test Neo4j JDBC Load`() {

        with(me.roybailey.research.kotlin.neo4j.Neo4jService) {
            init()
        }

        val csvTestData = File("./src/test/resources/testdata/").absolutePath+"/SampleCSVFile_53000kb.csv"
        val selectQuery = "SELECT * FROM CSVREAD('$csvTestData')"
        val cypherQuery = """CALL apoc.load.jdbc('jdbc:h2:mem:test;DB_CLOSE_DELAY=-1',"$selectQuery") YIELD row
            RETURN
                row.PRODUCT,
                custom.data.encrypt(row.FULLNAME),
                row.PRICE,
                row.UNITPRICE,
                apoc.text.toUpperCase(COALESCE(row.CATEGORY, "")),
                row.BRAND,
                row.QUANTITY,
                row.DISCOUNT
        """
        Neo4jService.runCypher(this::printResults, cypherQuery)

    }

    fun printResults(ctx: QueryResultContext, name: String, value: Any?) {
        if(ctx.column==0)
            println()
        print(" $name=${valueOf(value)}")
    }
}
