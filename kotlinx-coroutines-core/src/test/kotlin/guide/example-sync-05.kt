/*
 * Copyright 2016-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// This file was automatically generated from coroutines-guide.md by Knit tool. Do not edit.
package guide.sync.example05

import kotlinx.coroutines.experimental.*
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.experimental.channels.*

suspend fun massiveRun(action: suspend () -> Unit) {
    val n = 100_000
    val time = measureTimeMillis {
        val jobs = List(n) {
            launch(CommonPool) {
                action()
            }
        }
        jobs.forEach { it.join() }
    }
    println("Completed in $time ms")    
}

// Message types for counterActor
sealed class CounterMsg
object IncCounter : CounterMsg() // one-way message to increment counter
class GetCounter(val response: SendChannel<Int>) : CounterMsg() // a request with reply

// This function launches a new counter actor
fun counterActor(request: ReceiveChannel<CounterMsg>) = launch(CommonPool) {
    var counter = 0 // actor state
    while (isActive) { // main loop of the actor
        val msg = request.receive()
        when (msg) {
            is IncCounter -> counter++
            is GetCounter -> msg.response.send(counter)
        }
    }
}

fun main(args: Array<String>) = runBlocking<Unit> {
    val request = Channel<CounterMsg>()
    counterActor(request)
    massiveRun {
        request.send(IncCounter)
    }
    val response = Channel<Int>()
    request.send(GetCounter(response))
    println("Counter = ${response.receive()}")
}
