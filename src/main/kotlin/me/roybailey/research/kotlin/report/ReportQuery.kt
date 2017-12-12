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
        val name: String,
        val meta: List<ReportColumn>,
        val row: Int = -1,
        val column: Int = -1,
        val value: Any? = null
)


typealias ReportVisitor = (ctx: ReportContext) -> ReportContext


class CompositeReportVisitor(vararg args: ReportVisitor) {

    val visitors = mutableListOf(*args)

    fun reportVisit(ctxArg: ReportContext): ReportContext {
        var ctx = ctxArg
        visitors.forEach {
            ctx = it(ctx)
        }
        return ctx
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
            ctx
        }
        ReportEvent.START_ROW -> {
            data += mutableMapOf()
            ctx
        }
        ReportEvent.DATA -> {
            if (ctx.row == 0) {
                listColumns += ctx.name!!
                listColumnWidths += max(4, ctx.name!!.length)
            }
            data[ctx.row!!].put(listColumns[ctx.column!!], ctx.value)
            listColumnWidths[ctx.column] = max(listColumnWidths[ctx.column], valueOf(ctx.value).length)
            ctx
        }
        ReportEvent.END_ROW -> ctx
        ReportEvent.END_REPORT -> {
            log.info("$reportName${ctx.evt}")
            ctx
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

