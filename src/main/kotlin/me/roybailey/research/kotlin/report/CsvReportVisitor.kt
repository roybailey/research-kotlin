package me.roybailey.research.kotlin.report

import au.com.bytecode.opencsv.CSVWriter
import mu.KotlinLogging
import org.apache.commons.csv.CSVFormat
import java.io.Reader
import java.io.StringWriter
import java.io.Writer
import java.lang.String.valueOf


class CsvReportVisitor(
        val reportName: String,
        val delimiter: Char = ',',
        val writer: Writer = StringWriter()
) {

    private val log = KotlinLogging.logger {}

    val printer = CSVWriter(writer, delimiter)
    val listColumns = mutableListOf<String>()
    val listValues = mutableListOf<String>()

    fun reportVisit(ctx: ReportContext): ReportContext = when (ctx.evt) {
        ReportEvent.START_REPORT -> {
            log.info("$reportName${ctx.evt}")
            ctx
        }
        ReportEvent.DATA -> {
            if (ctx.row == 0) {
                listColumns += ctx.name!!
            }
            listValues += valueOf(ctx.value)
            ctx
        }
        ReportEvent.END_ROW -> {
            if (ctx.row == 0) {
                printer.writeNext(listColumns.toTypedArray())
            }
            printer.writeNext(listValues.toTypedArray())
            listValues.clear()
            ctx
        }
        ReportEvent.END_REPORT -> {
            log.info("$reportName${ctx.evt}")
            printer.flush()
            ctx
        }
        else -> ctx
    }

    override fun toString(): String = writer.toString()
}


class CsvReportReader {

    val listColumns = mutableListOf<String>()
    val data = mutableListOf<List<Any>>()

    fun read(reader: Reader): CsvReportReader {
        val records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader)
        listColumns.addAll(records.headerMap.keys)
        for (record in records) {
            val row = mutableListOf<Any>()
            record.forEach {
                row += valueOf(it)
            }
            data.add(row)
        }
        return this
    }
}

