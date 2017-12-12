package me.roybailey.research.kotlin.writers

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import java.nio.file.Files
import java.nio.file.Paths


class CsvTest {

    @Rule
    @JvmField
    val testName = TestName()

    @Test
    fun `Test Apache CSV`() {

        val filepath = Paths.get("./target/", testName.methodName.replace(' ','_')+".csv")

        Files.newBufferedWriter(filepath).use({ writer ->
            CSVPrinter(writer, CSVFormat.RFC4180
                    .withHeader("ID", "Name", "Designation", "Company")).use { csvPrinter ->
                csvPrinter.printRecord(1, "Sundar Pichai", "CEO", "Google")
                csvPrinter.printRecord(2, "Satya Nadella", "CEO", "Microsoft")
                csvPrinter.printRecord(3, "Tim cook", "CEO", "Apple")
                csvPrinter.printRecord(listOf(4, "Mark Zuckerberg", "CEO", "Facebook"))
                csvPrinter.flush()
            }
        })

        Files.newBufferedReader(filepath).use({ reader ->
            val records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader)
            assertThat(records.headerMap.keys).containsExactly("ID", "Name", "Designation", "Company")
            for (record in records) {
               assertThat(record).hasSize(4).contains("CEO")
            }
        })
    }
}
