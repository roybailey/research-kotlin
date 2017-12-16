package me.roybailey.research.kotlin.neo4j

import me.roybailey.research.kotlin.report.*
import mu.KotlinLogging
import org.neo4j.graphdb.Node


class Neo4jReportRunner(val neo4j: Neo4jService) : ReportRunner {

    private val log = KotlinLogging.logger {}


    override fun runReport(report: ReportDefinition, visitor: ReportVisitor) {

        //val visitor = CompositeReportVisitor(this::processNeo4jColumns, suppliedVisitor)::reportVisit
        var ctx = ReportContext(
                evt = ReportEvent.START_REPORT,
                name = report.reportName,
                meta = report.columns,
                row = -1, column = -1)
        ctx = visitor(ctx)

        neo4j.execute(report.query) { srs ->
            while (srs.hasNext()) {
                ctx = visitor(ctx.copy(evt = ReportEvent.START_ROW, row = ctx.row + 1, column = -1))
                val record = srs.next()
                srs.columns().forEachIndexed { cdx, name ->

                    val value = record.getValue(name)
                    ctx = ctx.copy(evt = ReportEvent.DATA)
                    when (value) {
                        is Node -> value.allProperties.forEach { prop ->
                            ctx = visitor(columnContext(report, ctx, name + "." + prop.key, value = prop.value))
                        }
                        is Map<*, *> -> value.keys.forEach { prop ->
                            ctx = visitor(columnContext(report, ctx, name + "." + prop, value[prop]))
                        }
                        else -> ctx = visitor(columnContext(report, ctx, name, value))
                    }

                }
                ctx = visitor(ctx.copy(evt = ReportEvent.END_ROW))
            }
            ctx = visitor(ctx.copy(evt = ReportEvent.END_REPORT, name = report.reportName))
        }
    }


    fun columnContext(def: ReportDefinition, ctx: ReportContext, name: String, value: Any?): ReportContext {
        var columnContext = ctx
        var meta = ctx.meta
        val column = ctx.column+1

        if (ctx.meta.size <= column)
            meta = ctx.meta.plus(ReportColumn(name))
        else if (ctx.meta[column].name != name) {
            ctx.meta.forEachIndexed { idx, col ->
                if (col.name == name)
                    columnContext = ctx.copy(column = idx)
            }
            if (columnContext.meta[columnContext.column].name != name)
                throw IllegalArgumentException("""Report column name mismatch: report=${def.reportName} columnIndex=${column} columnMeta=${ctx.meta[column].name} resultColumn=${name}""")
        }
        val resultContext = columnContext.copy(meta = meta, name = name, value = value, column = column)

        log.info(resultContext.toColumnString())
        return resultContext
    }
}
