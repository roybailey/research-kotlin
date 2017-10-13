package me.roybailey.research.kotlin.basic


fun main(args: Array<String>) {

    val exampleSet = hashSetOf(1, 5, 7)
    println(exampleSet)

    val exampleList = arrayListOf(1, 5, 7)
    println(exampleList)

    val exampleMap = hashMapOf(
            1 to "one",
            5 to "five",
            7 to "seven"
    )
    println(exampleMap)
}

