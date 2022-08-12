package me.roybailey.research.kotlin.util

import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object HttpUtil {

    private fun String.urlDecodeUTF8(): String = URLDecoder.decode(this, "UTF-8")

    @Suppress("UNCHECKED_CAST")
    private fun getValueList(value: Any): List<Any> =
        when (value) {
            is Array<*> -> listOf(*value) as List<Any>
            is List<*> -> value as List<Any>
            else -> listOf(value)
        }


    @Throws(UnsupportedEncodingException::class)
    fun parseQueryString(queryString: String): Map<String, List<String>> {
        val queryPairs = LinkedHashMap<String, ArrayList<String>>()

        queryString.split("&".toRegex())
            .dropLastWhile { it.isEmpty() }
            .map { it.split('=') }
            .map { it.getOrNull(0)!!.urlDecodeUTF8() to it.getOrNull(1)!!.urlDecodeUTF8() }
            .forEach { (key, multivalue) ->
                multivalue.split(",").forEach { value ->
                    if (!queryPairs.containsKey(key)) {
                        queryPairs[key] = arrayListOf(value)
                    } else {
                        if (!queryPairs[key]!!.contains(value)) {
                            queryPairs[key]!!.add(value)
                        }
                    }
                }
            }
        return queryPairs
    }


    fun createQueryString(parameterMap: Map<String, Any>): String = parameterMap.map { (key, value) ->
        "$key=" + getValueList(value).map {
            URLEncoder.encode(it.toString(), StandardCharsets.UTF_8.toString())
        }.joinToString(",")
    }.joinToString("&")

}
