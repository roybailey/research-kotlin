package me.roybailey.research.kotlin.basic

import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import java.lang.Integer.max
import java.lang.Integer.min


class KotlinCollections {

    @Test
    fun testKotlinListTransformations() {

        val listWords = listOf<String>(
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine",
            "ten",
        )
        val pageSize = 2
        val listPages = mutableListOf<List<String>>()
        for (index in listWords.indices step pageSize) {
            listPages.add(listWords.subList(index, min(listWords.size, index+pageSize)))
        }
        println(listPages.reversed().flatten())
    }

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
