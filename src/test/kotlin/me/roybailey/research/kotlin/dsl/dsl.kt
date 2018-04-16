package me.roybailey.research.kotlin.dsl

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


// ------------------------------------------------------------
// Data Model
// ------------------------------------------------------------


typealias QueryParams = Map<String, Any>
typealias QueryStatement = (params: QueryParams) -> String


data class SimpleQueryStatement(val query: String) {
    fun toQueryString(params: QueryParams): String = query
}

data class ApocJdbcLoad(val select: QueryStatement, val merge: QueryStatement) {
    fun toQueryString(params: QueryParams): String = "CALL apoc.load.jdbc(DB_URL,\"${select(params)}\") yield row\n${merge(params)}"
}

data class ApocJsonLoad(val uri: QueryStatement, val merge: QueryStatement) {
    fun toQueryString(params: QueryParams): String = "call apoc.load.json('${uri(params)}') yield row\n${merge(params)}"
}

data class ImportQueryScript(
        val deleteScript: List<QueryStatement>,
        val importScript: List<QueryStatement>,
        val enrichScript: List<QueryStatement>
)

data class ImportTask(val name: String, val steps: ImportQueryScript)


// ------------------------------------------------------------
// DSL Syntax Sugar
// ------------------------------------------------------------

fun importerTask(init: ImportTaskContext.() -> Unit): ImportTask {
    val context = ImportTaskContext().apply(init)
    return context.build()
}


class ImportTaskContext {

    var name: String? = null
    var query: ImportQueryScript = ImportQueryScript(emptyList(), emptyList(), emptyList())

    fun steps(init: ImportQueryScriptContext.() -> Unit) {
        val context = ImportQueryScriptContext().apply(init)
        query = context.build()
    }

    fun build() = ImportTask(this.name!!, query)
}


class ImportQueryScriptContext {

    private val deleteScript = mutableListOf<QueryStatement>()

    private val importScript = mutableListOf<QueryStatement>()

    private val enrichScript = mutableListOf<QueryStatement>()

    fun delete(init: QueryStatementContext.() -> Unit) {
        val context = QueryStatementContext().apply(init)
        deleteScript.addAll(context.build())
    }

    fun merge(init: QueryStatementContext.() -> Unit) {
        val context = QueryStatementContext().apply(init)
        importScript.addAll(context.build())
    }

    fun enrich(init: QueryStatementContext.() -> Unit) {
        val context = QueryStatementContext().apply(init)
        enrichScript.addAll(context.build())
    }

    fun build(): ImportQueryScript = ImportQueryScript(deleteScript, importScript, enrichScript)
}


class QueryStatementContext {

    var statement = mutableListOf<QueryStatement>()

    private fun splitSql(sqlScript: String) = sqlScript.split(Regex("--.*[\\n\\r]"))
    private fun splitCypher(cypherScript: String) = cypherScript.split(Regex("//.*[\\n\\r]"))

    fun QueryStatementContext.cypher(cypherScript: String) =
            statement.addAll(splitCypher(cypherScript)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { SimpleQueryStatement(it)::toQueryString }
                    .toList())

    fun QueryStatementContext.sql(sqlScript: String) =
            statement.addAll(splitSql(sqlScript)
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { SimpleQueryStatement(it)::toQueryString }
                    .toList())

    fun jdbc(init: ApocJdbcLoadStatementContext.() -> Unit) {
        val context = ApocJdbcLoadStatementContext().apply(init)
        statement.add(context.build()::toQueryString)
    }

    fun jdbc(select: String, merge: String) =
            jdbc {
                select(select)
                merge(merge)
            }

    fun json(init: ApocJsonLoadStatementContext.() -> Unit) {
        val context = ApocJsonLoadStatementContext().apply(init)
        statement.add(context.build()::toQueryString)
    }

    fun build() = statement
}


class ApocJdbcLoadStatementContext {

    var selectStatement: QueryStatement? = null
    var mergeStatement: QueryStatement? = null


    fun ApocJdbcLoadStatementContext.select(sql: String) {
        selectStatement = SimpleQueryStatement(sql)::toQueryString
    }

    fun ApocJdbcLoadStatementContext.merge(cypher: String) {
        mergeStatement = SimpleQueryStatement(cypher)::toQueryString
    }


    fun build() = ApocJdbcLoad(selectStatement!!, mergeStatement!!)
}


class ApocJsonLoadStatementContext {

    var uriStatement: QueryStatement? = null
    var mergeStatement: QueryStatement? = null


    fun ApocJsonLoadStatementContext.uri(uri: String) {
        uriStatement = SimpleQueryStatement(uri)::toQueryString
    }

    fun ApocJsonLoadStatementContext.merge(cypher: String) {
        mergeStatement = SimpleQueryStatement(cypher)::toQueryString
    }


    fun build() = ApocJsonLoad(uriStatement!!, mergeStatement!!)
}


// ------------------------------------------------------------
// Test DSL
// ------------------------------------------------------------

class Dsl2Test {

    @Test
    fun `test importer dsl sample`() {

        val actual = importerTask {
            name = "TestImport"
            steps {
                delete {
                    cypher("match (n) delete n")
                    cypher("drop index on :Label(name)")
                }
                merge {
                    jdbc {
                        select("select * from table")
                        merge("merge (n:Test {id: row.ID})")
                    }
                    //jdbc("select * from table", "merge (n:Test {id: row.ID})")
                }
                enrich {
                    cypher("""
                    // this is a comment
                    match (n:Test {status: 'ACTIVE'}) set n:Active

                    // this is another comment
                    match (n:Test {type: 'CLIENT'}) set n:Client

                    // this is even more comments
                    match (n:Test {type: 'PROSPECT'}) set n:Prospect
                    """.trimIndent())
                }
            }
        }

        val params = mapOf<String, Any>()

        assertThat(actual.name).isEqualTo("TestImport")
        assertThat(actual.steps.deleteScript).hasSize(2)
        assertThat(actual.steps.deleteScript[0](params)).isEqualTo("match (n) delete n")
        assertThat(actual.steps.importScript).hasSize(1)
        assertThat(actual.steps.importScript[0](params)).isEqualTo("""
                CALL apoc.load.jdbc(DB_URL,"select * from table") yield row
                merge (n:Test {id: row.ID})
                """.trimIndent())
        assertThat(actual.steps.enrichScript).hasSize(3)
        assertThat(actual.steps.enrichScript[0](params)).isEqualTo("match (n:Test {status: 'ACTIVE'}) set n:Active")
    }
}

