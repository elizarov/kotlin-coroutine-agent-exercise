package agent

import java.lang.instrument.Instrumentation

class Agent {
    companion object {
        @JvmStatic
        fun premain(agentArgs: String?, inst: Instrumentation) {
            println("Agent started.")
        }
    }
}
