package me.roybailey.research.kotlin.report

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import mu.KotlinLogging
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import java.io.OutputStream
import java.lang.String.valueOf


class ExcelReportVisitor(
        val reportName: String,
        val writer: OutputStream = ByteOutputStream()
) {

    private val log = KotlinLogging.logger {}

    // Capture the data
    val data = mutableListOf<MutableList<Any?>>()
    val listColumns = mutableListOf<Any?>()
    val listValues = mutableListOf<Any?>()

    fun reportVisit(ctx: ReportContext):ReportContext = when (ctx.evt) {
        ReportEvent.START_REPORT -> {
            log.info("$reportName ${ctx.evt}")
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
                data += listColumns.toMutableList()
            }
            while(listValues.size < listColumns.size)
                listValues += ""
            data += listValues.toMutableList()
            listValues.clear()
            ctx
        }
        ReportEvent.END_REPORT -> {
            log.info("$reportName ${ctx.evt}")
            writePdfReport()
            ctx
        }
        else -> ctx
    }

    fun writePdfReport() {

        val wb = HSSFWorkbook()
        val createHelper = wb.creationHelper
        val sheet = wb.createSheet(reportName)

        var rdx = 0
        var row = sheet.createRow(rdx++)
        listColumns.forEachIndexed { idx, name ->
            val cell = row.createCell(idx, CellType.STRING)
            cell.setCellValue(valueOf(name))
        }

        data.forEachIndexed { idx, values ->
            row = sheet.createRow(rdx++)
            values.forEachIndexed { idx, value ->
                val cell = row.createCell(idx, CellType.STRING)
                cell.setCellValue(valueOf(value))
            }
        }

        wb.write(writer)
        writer.flush()
    }

    override fun toString(): String = writer.toString()
}

