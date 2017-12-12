package me.roybailey.research.kotlin.neo4j

import me.roybailey.research.kotlin.report.*
import org.neo4j.graphdb.Node


class Neo4jReportRunner(val neo4j: Neo4jService) : ReportRunner {

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
                ctx = visitor(ctx.copy(evt = ReportEvent.START_ROW, row = ctx.row+1, column = -1))
                val record = srs.next()
                srs.columns().forEachIndexed { cdx, name ->

                    // validate or add column meta container for processing
                    if(ctx.meta.size <= cdx)
                        ctx = ctx.copy(meta = ctx.meta.plus(ReportColumn(name)))
                    else if(ctx.meta[cdx].name != name)
                        throw IllegalArgumentException("Report column name mismatch: report=${report.reportName} columnIndex=${cdx} columnMeta=${ctx.meta[cdx].name} resultColumn=${name}")

                    val value = record.getValue(name)
                    ctx = ctx.copy(evt = ReportEvent.DATA)
                    when (value) {
                        is Node -> value.allProperties.forEach { prop ->
                            ctx = visitor(ctx.copy(name = name + "." + prop.key, value = prop.value, column = ctx.column+1))
                        }
                        is Map<*, *> -> value.keys.forEach { prop ->
                            ctx = visitor(ctx.copy(name = name + "." + prop, value = value[prop], column = ctx.column+1))
                        }
                        else -> ctx = visitor(ctx.copy(name = name, value = value, column = ctx.column+1))
                    }
                }
                ctx = visitor(ctx.copy(evt = ReportEvent.END_ROW))
            }
            ctx = visitor(ctx.copy(evt = ReportEvent.END_REPORT))
        }
    }
}
