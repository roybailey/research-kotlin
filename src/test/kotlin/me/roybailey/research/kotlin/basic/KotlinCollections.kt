package me.roybailey.research.kotlin.basic

import org.assertj.core.api.SoftAssertions
import org.junit.Test


class KotlinCollections {

    @Test
    fun testKotlinMapTransformations() {

        val testMap = mapOf<String, String>(
                "one" to "one",
                "two" to "two",
                "three" to "three"
        )
        println(testMap)

        // transform keys only (use same values)
        val capsKeys = testMap.mapKeys { it.key.toUpperCase() }
        println(capsKeys)

        // transform values only (use same key) - what you're after!
        val capsVals = testMap.mapValues { it.value.toUpperCase() }
        println(capsVals)

        // transform keys + values
        val capsBoth = testMap.entries.associate { it.key.toUpperCase() to it.value.toUpperCase() }
        println(capsBoth)

        val setLowerCase = setOf("one", "two", "three")
        val setUpperCase = setOf("ONE", "TWO", "THREE")
        SoftAssertions().run {
            assertThat(capsKeys.keys).isEqualTo(setUpperCase)
            assertThat(capsKeys.values).isEqualTo(setLowerCase)
            assertThat(capsVals.keys).isEqualTo(setLowerCase)
            assertThat(capsVals.values).isEqualTo(setUpperCase)
            assertThat(capsBoth.keys).isEqualTo(setUpperCase)
            assertThat(capsBoth.values).isEqualTo(setUpperCase)
        }
    }
}