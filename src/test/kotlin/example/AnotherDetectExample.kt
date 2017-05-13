package example

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking

fun main(args: Array<String>) = runBlocking<Unit> {
    println("Started!")

    AnotherDetectExample.test()
    AnotherDetectExample.Fake.test()
    AnotherDetectExample.test(null)
    AnotherDetectExample.test()
    AnotherDetectExample.Fake.test()
    AnotherDetectExample.test(null)
    AnotherDetectExample.foo()
    AnotherDetectExample.test()
    AnotherDetectExample.Fake.test()
    AnotherDetectExample.test(null)
    AnotherDetectExample.test()
    AnotherDetectExample.foo()
    AnotherDetectExample.Fake.test()
    AnotherDetectExample.test(null)
    AnotherDetectExample.Fake.test()

    println("Done.")
}

class AnotherDetectExample {
    companion object {
        suspend fun foo() {
            test()
            Fake.test()
            test(null)
            test()
            Fake.test()
            test(null)
        }

        suspend fun test() {
            println("True test")
            delay(1)
        }

        suspend fun test(fake: Any?) {
            println("Another fake test")
        }
    }

    class Fake {
        companion object {
            @JvmStatic
            suspend fun test() {
                println("Fake test")
            }
        }
    }
}