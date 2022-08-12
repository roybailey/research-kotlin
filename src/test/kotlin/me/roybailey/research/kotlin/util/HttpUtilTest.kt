package me.roybailey.research.kotlin.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll


class HttpUtilTest {

    @Test
    fun testParseQueryString():Unit {
        val originalQueryString = arrayOf(
            "abc.equal=xyz",
            "abc.lessthanequal=100",
            "abc.like=mickey+%26+mouse",
            "abc.in=def,ghi",
            "startDate,endDate.between=2022-01-01,2023-01-01")
            .joinToString("&")

        // check query string to parameter map...
        val parameterMap = HttpUtil.parseQueryString(originalQueryString)
        assertThat(parameterMap.size).isEqualTo(5)
        assertAll(originalQueryString,
            { assertThat(parameterMap["abc.equal"]).isEqualTo(listOf("xyz")) },
            { assertThat(parameterMap["abc.lessthanequal"]).isEqualTo(listOf("100")) },
            { assertThat(parameterMap["abc.like"]).isEqualTo(listOf("mickey & mouse")) },
            { assertThat(parameterMap["abc.in"]).isEqualTo(listOf("def","ghi")) },
            { assertThat(parameterMap["startDate,endDate.between"]).isEqualTo(listOf("2022-01-01","2023-01-01")) },
        )

        // check parameter map to query string...
        val paramsToQueryString = HttpUtil.createQueryString(parameterMap)
        assertThat(paramsToQueryString).isEqualTo(originalQueryString)
    }

}
