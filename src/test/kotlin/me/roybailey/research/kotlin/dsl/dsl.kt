package me.roybailey.research.kotlin.dsl

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


// ------------------------------------------------------------
// Data Model
// ------------------------------------------------------------

interface ImportStatement {

    fun toQueryString(): String
}

data class SimpleQuery(val query: String) : ImportStatement {
    override fun toQueryString(): String = query
}

data class JdbcLoad(val select: String, val merge: String) : ImportStatement {
    override fun toQueryString(): String = "call load.jdbc('$select') yield row\n$merge"
}

data class JsonLoad(val uri: String, val merge: String) : ImportStatement {
    override fun toQueryString(): String = "call load.json('$uri') yield row\n$merge"
}

data class ImportQuery(
        val deleteScript: List<ImportStatement>,
        val importScript: List<ImportStatement>,
        val enrichScript: List<ImportStatement>
)

data class ImportJob(val name: String, val steps: ImportQuery)


// ------------------------------------------------------------
// DSL Syntax Sugar
// ------------------------------------------------------------

fun importerJob(init: ImportJobContext.() -> Unit): ImportJob {
    val context = ImportJobContext().apply(init)
    return context.build()
}

class ImportJobContext {

    var name: String? = null
    var query: ImportQuery = ImportQuery(emptyList(), emptyList(), emptyList())

    fun steps(init: ImportQueryContext.() -> Unit) {
        val context = ImportQueryContext().apply(init)
        query = context.build()
    }

    fun build() = ImportJob(this.name!!, query)
}

class ImportQueryContext {

    private val deleteScript = mutableListOf<ImportStatement>()

    private val importScript = mutableListOf<ImportStatement>()

    private val enrichScript = mutableListOf<ImportStatement>()

    fun delete(init: ImportStatementContext.() -> Unit) {
        val context = ImportStatementContext().apply(init)
        deleteScript.addAll(context.build())
    }

    fun merge(init: ImportStatementContext.() -> Unit) {
        val context = ImportStatementContext().apply(init)
        importScript.addAll(context.build())
    }

    fun enrich(init: ImportStatementContext.() -> Unit) {
        val context = ImportStatementContext().apply(init)
        enrichScript.addAll(context.build())
    }

    fun build(): ImportQuery = ImportQuery(deleteScript, importScript, enrichScript)
}

class ImportStatementContext {

    var statement = mutableListOf<ImportStatement>()

    private fun split(cypherScript: String) = cypherScript.split(Regex("//.*[\\n\\r]"))

    fun cypher(cypherScript: String) {
        split(cypherScript)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach {
                    statement.add(SimpleQuery(it))
                }
    }

    fun jdbc(select: String, merge: String) {
        statement.add(JdbcLoad(select, merge))
    }

    fun json(uri: String, merge: String) {
        statement.add(JsonLoad(uri, merge))
    }

    fun build() = statement
}


// ------------------------------------------------------------
// Test DSL
// ------------------------------------------------------------

class DslTest {

    @Test
    fun `test importer dsl sample`() {

        val actual = importerJob {
            name = "TestImport"
            steps {
                delete {
                    cypher("match (n) delete n")
                    cypher("drop index on :Label(name)")
                }
                merge {
                    jdbc("select * from table", "merge (n:Test {id: row.ID})")
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

        assertThat(actual.name).isEqualTo("TestImport")
        assertThat(actual.steps.deleteScript).hasSize(2)
        assertThat(actual.steps.deleteScript[0].toQueryString()).isEqualTo("match (n) delete n")
        assertThat(actual.steps.importScript).hasSize(1)
        assertThat(actual.steps.importScript[0].toQueryString()).isEqualTo("""
                call load.jdbc('select * from table') yield row
                merge (n:Test {id: row.ID})
                """.trimIndent())
        assertThat(actual.steps.enrichScript).hasSize(3)
        assertThat(actual.steps.enrichScript[0].toQueryString()).isEqualTo("match (n:Test {status: 'ACTIVE'}) set n:Active")
    }
}

