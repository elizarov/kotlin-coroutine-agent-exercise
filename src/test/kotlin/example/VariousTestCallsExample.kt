package example

import kotlinx.coroutines.experimental.*

class CuriousTestCaller {
    companion object {
        var z : Int = 0

        init {
            runBlocking {
                z = 6
                test()
                z = 8
            }
        }
    }

    init {
        runBlocking { test() }
    }

    fun pseudoFib(n : Int, a : Int, b : Int) : Int {
        var curA = a
        var curB = b
        val defs: MutableList<Deferred<Unit>> = arrayListOf()
        for (i in 0..n) {
            val tmp = curB
            curB = curA + curB
            defs.add(async(CommonPool) { test() })
            curA = tmp
        }

        runBlocking {
            for (def in defs) {
                def.await()
            }

            val job = launch(CommonPool) { try{ delay(1234); println("here in try") } finally {
                run(NonCancellable) {
                    println("in finally")
                    test()
                    println("leaving finally")
                }
            } }
            job.cancel()
            delay(1234)
        }

        return curB
    }
}

fun main(args: Array<String>) {
    // Perform calls to `test` in richer context.
    val inst = CuriousTestCaller()
    inst.pseudoFib(10, 0, 1)
}