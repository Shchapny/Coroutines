/*
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.coroutines.EmptyCoroutineContext

fun main() {
    println(1)
    val executor = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
    with(CoroutineScope(EmptyCoroutineContext)) {
        launch(Dispatchers.Default) {
            println(Thread.currentThread().name)
        }
        launch(Dispatchers.IO) {
            println(Thread.currentThread().name)
        }
        repeat(10) {
            launch(executor) {
                println(Thread.currentThread().name)
            }
        }
    }

    Thread.sleep(1000L)
    executor.close()
    println(2)
}

 */