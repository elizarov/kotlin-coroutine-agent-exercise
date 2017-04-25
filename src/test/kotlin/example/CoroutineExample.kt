package example

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking

fun main(args: Array<String>) = runBlocking<Unit> {
    println("Started!")
    test()
    println("Done.")
}

suspend fun test() {
    delay(1000)
}
