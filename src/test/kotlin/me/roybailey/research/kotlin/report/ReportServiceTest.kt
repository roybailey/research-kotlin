package me.roybailey.research.kotlin.report

import com.google.common.net.MediaType
import me.roybailey.research.kotlin.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader


class ReportDefinitionTest : BaseServiceTest() {

    @Test
    fun `Test Simple CSV File`(testInfo: TestInfo) {

        val report = ReportDefinition(testInfo.displayName,
                QueryType.NEO4J,
                """
                    CALL apoc.load.jdbc('jdbc:h2:mem:test;DB_CLOSE_DELAY=-1',"SELECT * FROM CSVREAD('${File("./src/test/resources/testdata/SampleCSVFile_2kb.csv").absolutePath}')") YIELD row
                    RETURN
                    row.ID as ID,
                    row.PRODUCT as PRODUCT,
                    custom.data.encrypt(row.FULLNAME) as FULLNAME,
                    row.PRICE as PRICE,
                    row.UNITPRICE as UNITPRICE,
                    apoc.text.toUpperCase(COALESCE(row.CATEGORY, "")) as CATEGORY,
                    row.BRAND as BRAND,
                    row.QUANTITY as QUANTITY,
                    row.DISCOUNT as DISCOUNT
                """.trimIndent())

        val reportName = testInfo.displayName
        val file = File("./target/" + reportName.replace(' ', '_') + ".csv")

        file.delete()
        val csvOutput = ReportOutput(
                MediaType.CSV_UTF_8.toString(),
                reportName,
                FileOutputStream(file)
        )

        reportService.runReport(report, listOf(csvOutput))

        assertThat(file.exists()).isTrue()
        val csv = CsvReportReader().read(FileReader(file))
        assertThat(csv.listColumns).hasSize(9)
        assertThat(csv.data[0])
                .hasSize(9)
                .contains("TXVoYW1tZWQgTWFjSW50eXJl") // base64 encoded name
                .contains("STORAGE_ORGANIZATION") // uppercase category
                .contains("3") // quantity
    }


    @Test
    fun `Test Simple CSV File With Columns`(testInfo: TestInfo) {

        val report = ReportDefinition(testInfo.displayName,
                queryType = QueryType.NEO4J,
                query = """
                    CALL apoc.load.jdbc('jdbc:h2:mem:test;DB_CLOSE_DELAY=-1',"SELECT * FROM CSVREAD('${File("./src/test/resources/testdata/SampleCSVFile_2kb.csv").absolutePath}')") YIELD row
                    RETURN
                    row.ID as ID,
                    row.PRODUCT as PRODUCT,
                    custom.data.encrypt(row.FULLNAME) as FULLNAME,
                    row.PRICE as PRICE,
                    row.UNITPRICE as UNITPRICE,
                    apoc.text.toUpperCase(COALESCE(row.CATEGORY, "")) as CATEGORY,
                    row.BRAND as BRAND,
                    row.QUANTITY as QUANTITY,
                    row.DISCOUNT as DISCOUNT
                """.trimIndent(),
                columns = listOf(
                        ReportColumn("ID"),
                        ReportColumn("PRODUCT"),
                        ReportColumn("FULLNAME", type = "BASE64:DECODE"),
                        ReportColumn("PRICE"),
                        ReportColumn("UNITPRICE"),
                        ReportColumn("CATEGORY"),
                        ReportColumn("BRAND"),
                        ReportColumn("QUANTITY", Int::class.java.simpleName),
                        ReportColumn("DISCOUNT")
                        )
        )

        val reportName = testInfo.displayName
        val file = File("./target/" + reportName.replace(' ', '_') + ".csv")

        file.delete()
        val csvOutput = ReportOutput(
                MediaType.CSV_UTF_8.toString(),
                reportName,
                FileOutputStream(file)
        )

        reportService.runReport(report, listOf(csvOutput))

        assertThat(file.exists()).isTrue()
        val csv = CsvReportReader().read(FileReader(file))
        assertThat(csv.listColumns).hasSize(9)
        assertThat(csv.data[0])
                .hasSize(9)
                .contains("Muhammed MacIntyre") // base64 encoded name decoded while processing
                .contains("STORAGE_ORGANIZATION") // uppercase category
                // .contains(3) // quantity as number (disabled as always read as String)
    }
}

