package me.roybailey.research.kotlin.h2

import mu.KotlinLogging
import org.junit.Test
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException


class H2Test {

    private val log = KotlinLogging.logger {}

    val DB_DRIVER = "org.h2.Driver"
    val DB_CONNECTION = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    val DB_USER = ""
    val DB_PASSWORD = ""

    private fun getDBConnection(): Connection? {
        var dbConnection: Connection? = null
        try {
            Class.forName(DB_DRIVER)
        } catch (e: ClassNotFoundException) {
            println(e.message)
        }

        try {
            dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD)
            return dbConnection
        } catch (e: SQLException) {
            println(e.message)
        }

        return dbConnection
    }

    @Test
    fun `Test H2 in-memory database and CSV load`() {

        val connection = getDBConnection()
        var selectPreparedStatement: PreparedStatement? = null

        val csvTestData = File("./src/test/resources/testdata/").absolutePath+"/SampleCSVFile_53000kb.csv"
        val SelectQuery = "SELECT * FROM CSVREAD('$csvTestData')"

        log.info { SelectQuery }

        try {
            connection!!.autoCommit = false

            selectPreparedStatement = connection.prepareStatement(SelectQuery)
            val rs = selectPreparedStatement!!.executeQuery()
            while (rs.next()) {
                for(col in 1..rs.metaData.columnCount) {
                    print(" ${rs.metaData.getColumnName(col)}=${rs.getObject(col)}")
                }
                println()
            }
            selectPreparedStatement.close()

            connection.commit()
        } catch (e: SQLException) {
            println("Exception Message " + e.localizedMessage)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection!!.close()
        }
    }
}
