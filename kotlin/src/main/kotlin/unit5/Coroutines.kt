package org.example.unit5

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

fun main() {
    println("Start of main")
    val elapsed = measureTimeMillis {
        runBlocking {
            println("Weather Forecast")
            launch {
                getForecast(duration = 5_000)
            }
            launch {
                getTemperature(duration = 500)
            }
            println("have a good day")
        }
    }
    println("Execution took $elapsed ms (${ elapsed / 1000 } seconds)")
    println("End of main")
}

suspend fun getForecast(duration: Long = 1_000): String {
    delay(duration)
    return "It's sunny outside!"
}

suspend fun getTemperature(duration: Long = 1_000): String {
    delay(duration)
    throw AssertionError("Temperature is invalid")
    return "The temperature is 30 \u00b0C"
}

suspend fun getTemperature_Throws(duration: Long = 1_000): String {
    delay(duration)
    return "The temperature is 30 \u00b0C"
}

suspend fun getWeatherReport(): String = coroutineScope {
    val forecast: Deferred<String> = async { getForecast(duration = 5_000) }
    val temperature: Deferred<String> = async { getTemperature(duration = 3_000) }
    "${ forecast.await() } - ${ temperature.await()}"
}
