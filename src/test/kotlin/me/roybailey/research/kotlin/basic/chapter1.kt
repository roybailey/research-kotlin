package me.roybailey.research.kotlin.basic

import java.io.BufferedReader
import java.io.StringReader
import java.util.*


enum class Color { RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET }

enum class RGB(val red: Int, val green: Int, val blue: Int) {
    RED(255, 0, 0),
    ORANGE(255, 165, 0),
    YELLOW(255, 255, 0),
    GREEN(0, 255, 0),
    BLUE(0, 0, 255),
    INDIGO(75, 0, 130),
    VIOLET(238, 130, 238);

    fun rgb() = (red * 256 + green) * 256 + blue
}

fun main(args: Array<String>) {

    // shows code within string...
    println("Hello, ${if (args.isNotEmpty()) args[0] else "World"}!")

    // shows function
    fun max(a: Int, b: Int) = if (a > b) a else b

    val a = 20
    val b = 40
    println("max($a,$b)=" + max(a, b))

    // show data class with custom field
    data class MyRectangle(val height: Int, val width: Int) {
        val isSquare: Boolean
            get() = height == width
    }

    val shouldBeSquare = MyRectangle(4, 4)
    val shouldBeRectangular = MyRectangle(6, 4)

    println("square $shouldBeSquare " + shouldBeSquare.isSquare)
    println("rectangle $shouldBeRectangular " + shouldBeRectangular.isSquare)

    // shows enum use-cases
    println(Color.BLUE)
    println(RGB.VIOLET.rgb())

    fun getRGBLetter(color: Color) =
            when (color) {
                Color.RED, Color.ORANGE, Color.YELLOW -> "warm"
                Color.BLUE, Color.INDIGO, Color.VIOLET -> "cold"
                Color.GREEN -> "neutral"
                else -> "?"
            }
    println(getRGBLetter(Color.BLUE))

    fun getColorMix(c1: Color, c2: Color) =
            when (setOf(c1, c2)) {
                setOf(Color.RED, Color.YELLOW) -> Color.ORANGE
                setOf(Color.BLUE, Color.YELLOW) -> Color.GREEN
                setOf(Color.BLUE, Color.VIOLET) -> Color.INDIGO
                else -> "?"
            }
    println(getColorMix(Color.BLUE, Color.YELLOW))

    // fizzbuzz range loops, when without params (thus boolean statements used)
    fun fizzbuzz(i: Int) =
            when {
                (i % 15 == 0) -> "FizzBuzz "
                (i % 3 == 0) -> "Fizz "
                (i % 5 == 0) -> "Buzz "
                else -> "$i "
            }
    for (i in 1..30) // inclusive
        print(fizzbuzz(i))
    println()

    for (i in 1 until 30) // exclusive
        print(fizzbuzz(i))
    println()

    for (i in 30 downTo 1)
        print(fizzbuzz(i))
    println()

    for (i in 1 until 30 step 3)
        print(fizzbuzz(i))
    println()

    // iterating over maps
    val mapHexChars = TreeMap<Char, String>()

    for (c in 'A'..'Z')
        mapHexChars[c] = Integer.toHexString(c.toInt())

    for ((letter, hex) in mapHexChars)
        print("$letter = $hex | ")
    println()

    // list loops with index
    val listHexCodes = arrayListOf("4f", "41", "5a")
    for ((index, element) in listHexCodes.withIndex())
        println("$index: $element")

    // in and !in for range checks
    fun isLetter(c: Char) = c in 'a'..'z' || c in 'A'..'Z'

    fun isNotDigit(c: Char) = c !in '0'..'9'
    println("q isLetter = " + isLetter('q'))
    println("x isNotDigit = " + isNotDigit('x'))

    fun recognize(c: Char) = when (c) {
        in '0'..'9' -> "it's a digit"
        in 'A'..'Z', in 'a'..'z' -> "it's a letter"
        else -> "don't know"
    }
    println(recognize('8'))
    println(recognize('R'))
    println(recognize('s'))
    println(recognize('#'))

    // object ranges use Comparable
    println("Kotlin" in "Java".."Scala") // true as Java <= Kotlin && Kotlin <= Scala
    println("Kotlin" in setOf("Java","Scala")) // false as doesn't match

    // try/catch blocks are an expression returning value, hence catch must return something
    fun readInteger(reader: BufferedReader) {
        val number = try {
            Integer.parseInt(reader.readLine())
        } catch (e: NumberFormatException) {
            null
        }
        println(number)
    }
    readInteger(BufferedReader(StringReader("Not a number")))
}

