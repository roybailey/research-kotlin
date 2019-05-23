package me.roybailey.research.kotlin.basic


/**
 * https://kotlinexpertise.com/coping-with-kotlins-scope-functions/
 */
fun main(args: Array<String>) {

    run {
        val buffer = StringBuffer("this is a message")

        println()
        println("inline fun <T,R> T.let( block:(T)->R ) : R")
        println("'let' executes a lambda with object as 'it' and returns arbitrary result type")
        val message = buffer.let {
            println("'let's do arbitrary stuff here with '$it'")
            it.append(" hello").append(" 'let'").toString()
        }

        println(message)
    }

    println()
    println("inline fun <T,R> T.run( block:T.()->R ) : R")
    run {
        val buffer = StringBuffer("this is a message")

        println("'run' executes a lambda with object as 'receiver' (so 'this' is assigned) and returns arbitrary result type")
        val message = buffer.run {
            println("'let's do arbitrary stuff here with '$this'")
            append(" hello") // no 'this' or 'it' required
            append(" 'run'")
            toString()
        }

        println(message)
    }

    println()
    println("inline fun T.also( block:(T)->Unit ) : T")
    run {
        val buffer = StringBuffer("this is a message")

        println("'also' executes a lambda with object as 'it' and returns this regardless of code block")
        val message = buffer.also {
            println("'also' do arbitrary stuff here with '$it'")
            it.append(" hello")
            it.toString() // makes no difference as also always returns the original object
        }.also {
            it.append(" 'also'")
        }

        println(message)
    }

    println()
    println("inline fun T.apply(block: T.() -> Unit): T")
    run {
        val buffer = StringBuffer("this is a message")

        println("'apply' executes a lambda with object as 'receiver' (so 'this' is assigned) and returns this regardless of code block")
        val message = buffer.apply {
            println("'also' do arbitrary stuff here with '$this'")
            append(" hello")
            toString() // makes no difference as also always returns the original object
        }.apply {
            append(" 'apply'")
        }

        println(message)
    }

    println()
    println("inline fun <T, R> with(receiver: T, block: T.() -> R): R")
    run {
        println("'with' executes a lambda with parameter object as 'receiver' (so 'this' is assigned) and returns arbitrary result")
        val message = with(StringBuffer("this is a message")) {
            println("'with' do arbitrary stuff here with '$this'")
            append(" hello")
            append(" 'with'")
            toString()
        }

        println(message)
    }

    println()
    println()
}