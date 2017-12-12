package me.roybailey.research.kotlin.report

import mu.KotlinLogging
import org.bouncycastle.util.encoders.Base64
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

object StandardReportVisitor {
    fun decoders(ctx: ReportContext): ReportContext = when (ctx.evt) {
        ReportEvent.DATA -> {
            when(ctx.meta[ctx.column].format) {
                "BASE64:DECODER" -> ctx.copy(value = Base64.decode(valueOf(ctx.value)))
            }
            ctx
        }
        else -> ctx
    }
}

class CompositeReportVisitor(vararg args: ReportVisitor) {

    val visitors = mutableListOf(StandardReportVisitor::decoders, *args)
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

