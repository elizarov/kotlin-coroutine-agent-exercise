package example

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.CommonPool

fun main(args: Array<String>) = runBlocking<Unit> {
    println("Started!")
    val x = async(CommonPool) { test() }
    val y = async(CommonPool) { test() }
    val z = async(CommonPool) { test() }
    x.await()
    y.await()
    z.await()
    println("Done.")
}

suspend fun test() {
    delay(1000)
}
