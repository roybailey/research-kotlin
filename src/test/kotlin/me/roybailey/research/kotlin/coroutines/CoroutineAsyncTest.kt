import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis


class CoroutineAsyncTest {

    suspend fun doSomethingUsefulOne(): Int {
        delay(1000L) // pretend we are doing something useful here
        return 13
    }

    suspend fun doSomethingUsefulTwo(): Int {
        delay(1000L) // pretend we are doing something useful here, too
        return 29
    }

    @Test
    fun testSequentialByDefault() = runBlocking<Unit> {
        println("testSequentialByDefault started")
        launch {
            val time = measureTimeMillis {
                val one = doSomethingUsefulOne()
                val two = doSomethingUsefulTwo()
                println("The answer is ${one + two}")
            }
            println("Completed in $time ms")
        }
    }


    // Conceptually, async is just like launch.
    // It starts a separate coroutine which is a light-weight thread that works concurrently with all the other coroutines.
    // The difference is that launch returns a Job and does not carry any resulting value,
    // while async returns a Deferred — a light-weight non-blocking future that represents a promise to provide a result later.
    // You can use .await() on a deferred value to get its eventual result, but Deferred is also a Job, so you can cancel it if needed.

    // NOTE: if something goes wrong inside the code of the concurrentSumOf function and it throws an exception,
    // all the coroutines that were launched in its coroutineScope {} will be cancelled.
    private suspend fun concurrentSumOf(): Int = coroutineScope {
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        one.await() + two.await()
    }

    @Test
    fun testConcurrencyUsingAsync() = runBlocking<Unit> {
        println("testConcurrencyUsingAsync started")
        launch {
            val time = measureTimeMillis {
                println("The answer is ${concurrentSumOf()}")
            }
            println("Completed in $time ms")
        }
    }

}
