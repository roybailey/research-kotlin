package me.roybailey.research.kotlin.report

import mu.KotlinLogging
import java.lang.Math.max
import java.lang.String.valueOf


enum class ReportEvent {
    START_REPORT,
    START_ROW,
    DATA,
    END_ROW,
    END_REPORT
}


data class ReportContext(
        val evt: ReportEvent,
        val name: String? = null,
        val value: Any? = null,
        val row: Int? = null,
        val column: Int? = null
)


typealias ReportVisitor = (ctx: ReportContext) -> Unit


class CompositeReportVisitor(vararg args: ReportVisitor) {

    val visitors = mutableListOf(*args)

    fun reportVisit(ctx: ReportContext) {
        visitors.forEach {
            it(ctx)
        }
    }
}


class SimpleReportVisitor(val reportName: String) {

    private val log = KotlinLogging.logger {}

    val listColumns = mutableListOf<String>()
    val listColumnWidths = mutableListOf<Int>()
    val data = mutableListOf<MutableMap<String, Any?>>()

    fun reportVisit(ctx: ReportContext) = when (ctx.evt) {
        ReportEvent.START_REPORT -> {
            log.info("$reportName${ctx.evt}")
        }
        ReportEvent.START_ROW -> {
            data += mutableMapOf()
        }
        ReportEvent.DATA -> {
            if (ctx.row == 0) {
                listColumns += ctx.name!!
                listColumnWidths += max(4, ctx.name!!.length)
            }
            data[ctx.row!!].put(listColumns[ctx.column!!], ctx.value)
            listColumnWidths[ctx.column] = max(listColumnWidths[ctx.column], valueOf(ctx.value).length)
            Unit
        }
        ReportEvent.END_ROW -> {
        }
        ReportEvent.END_REPORT -> {
            log.info("$reportName${ctx.evt}")
        }
    }

    override fun toString(): String {
        val buffer = StringBuffer()
        with(buffer) {
            append(" | ")
            listColumns.forEachIndexed { idx, column ->
                append(valueOf(column).padEnd(listColumnWidths[idx])).append(" | ")
            }
            append("\n")
            data.forEach {
                append(" | ")
                listColumns.forEachIndexed { idx, column ->
                    append(valueOf(it[column]).padEnd(listColumnWidths[idx])).append(" | ")
                }
                append("\n")
            }
        }
        return buffer.toString()
    }
}

