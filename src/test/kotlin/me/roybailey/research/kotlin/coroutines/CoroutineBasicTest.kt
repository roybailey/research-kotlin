import kotlinx.coroutines.*
import org.junit.jupiter.api.Test


class CoroutineBasicTest {

    @Test
    fun testRunBlockingBlock() {

        // thread invoking runBlocking blocks until the coroutine inside runBlocking completes...
        runBlocking<Unit> { // start main coroutine
            GlobalScope.launch { // launch a new coroutine in background and continue
                delay(1000L)
                println("testRunBlockingBlock!")
            }
            print("Hello,") // main coroutine continues here immediately
            delay(2000L)      // delaying for 2 seconds to keep JVM alive
        }
    }


    // thread invoking runBlocking methods until the coroutine inside runBlocking completes...
    @Test
    fun testRunBlockingMethod() = runBlocking<Unit> {

        GlobalScope.launch { // launch a new coroutine in background and continue
            delay(1000L)
            println("testRunBlockingMethod!")
        }
        print("Hello,") // main coroutine continues here immediately
        delay(2000L)      // delaying for 2 seconds to keep JVM alive
    }


    // thread invoking runBlocking methods until the coroutine inside runBlocking completes...
    @Test
    fun testJoiningCoroutine() = runBlocking<Unit> {

        val job = GlobalScope.launch { // launch a new coroutine in background and continue
            delay(1000L)
            println("testJoiningCoroutine!")
        }
        print("Hello,") // main coroutine continues here immediately
        job.join()      // delaying for 2 seconds to keep JVM alive
    }

    // thread invoking runBlocking methods until the coroutine inside runBlocking completes...
    @Test
    fun testLocalCoroutine() = runBlocking<Unit> {

        launch { // launch a new coroutine in background and continue
            delay(1000L)
            println("testLocalCoroutine!")
        }
        print("Hello,") // main coroutine continues here immediately
        // don't need to join as launch is local to this runBlocking block
        // so the outer runBlocking method will not complete until all
        // internal coroutines complete
    }

    @Test
    fun testCoroutineScope() = runBlocking<Unit> {
        launch {
            delay(200L)
            println("Task from runBlocking")
        }

        coroutineScope { // Creates a coroutine scope
            launch {
                delay(500L)
                println("Task from nested launch")
            }

            delay(100L)
            println("Task from coroutine scope") // This line will be printed before the nested launch
        }

        println("Coroutine scope is over") // This line is not printed until the nested launch completes
    }


    @Test
    fun testSuspendMethods() = runBlocking {
        launch { doCoroutineCode() }
        print("Hello,")
    }

    // this is your first suspending function
    private suspend fun doCoroutineCode() {
        delay(1000L)
        println("testSuspendMethods!")
    }


    @Test
    fun testLightWeightRepeats() = runBlocking {
        // launch a lot of coroutines, not possible with threads...
        repeat(100_000) {
            launch {
                delay(1000L)
                print(".")
            }
        }
    }


    @Test
    fun testCancellation() = runBlocking {
        val job = launch {
            try {
                repeat(1000) { i ->
                    println("job: I'm sleeping $i ...")
                    delay(500L)
                }
            } finally {
                println("job: finally block executing")
            }
        }
        delay(1300L) // delay a bit
        println("main: I'm tired of waiting!")
        job.cancelAndJoin() // cancels the job and waits for job's completion
        println("main: Now I can quit.")
    }


    @Test
    fun testTimeout() = runBlocking {
        val job = launch {
            try {
                withTimeout(1000) {
                    repeat(1000) { i ->
                        println("job: I'm sleeping $i ...")
                        delay(500L)
                    }
                }
            } catch (err: Exception) {
                println("job: "+err.message)
            } finally {
                println("job: finally block executing")
            }
        }
        println("main: Now I can quit.")
    }
}
