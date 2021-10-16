package me.roybailey.research.kotlin.reflection

import org.assertj.core.api.Assertions.assertThat
import org.bouncycastle.asn1.x500.style.RFC4519Style.street
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.Instant.now
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberExtensionFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import org.assertj.core.api.SoftAssertions
import kotlin.reflect.KParameter
import kotlin.reflect.KType


class ReflectionTest {

    data class Record(
            val text: String,
            val flag: Boolean,
            val time: Instant
    ) {
        // example alternative constructors to see how to handle in reflection
        constructor(text: String) : this(text, true, now())
        constructor(flag: Boolean) : this("Flag Construction", flag, now())
        constructor(time: Instant) : this("Time Construction", true, time)
    }

    private val recordClass = Record::class
    private val recordSample: Record = Record("hello data class", true, now())

    @Test
    fun testPropertyReflectionOfDataClass() {

        println(recordClass.constructors)
        println(recordClass.functions)
        println(recordClass.memberProperties)
        println(recordClass.memberExtensionFunctions)

        println()
        println()
        recordClass.constructors.forEach { init ->
            println("${init.name} of type ${init.returnType} of visibility ${init.visibility}")
            init.parameters.forEach { param ->
                println("    ${param.name} of type ${param.type} of kind ${param.kind}")
            }
        }

        println()
        println()
        recordClass.memberProperties.forEach {
            println("${it.name} of type ${it.returnType} of visibility ${it.visibility}")
        }
    }
}
