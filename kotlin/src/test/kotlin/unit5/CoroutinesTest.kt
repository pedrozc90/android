package unit5

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.example.unit5.getForecast
import org.example.unit5.getTemperature
import org.example.unit5.getWeatherReport
import kotlin.system.measureTimeMillis
import kotlin.test.Test

class CoroutinesTest {

    @Test
    fun testSyncronous() {
        println("Start of main")
        val elapsed = measureTimeMillis {
            runBlocking {
                println("Weather Forecast")
                launch {
                    val result = getForecast(duration = 5_000)
                    println(result)
                }
                launch {
                    val result = getTemperature(duration = 3_000)
                    println(result)
                }
                println("have a good day")
            }
        }
        println("Execution took $elapsed ms (${ elapsed / 1000 } seconds)")
        println("End of main")
    }

    @Test
    fun testAshyncronous() {
        println("Start of main")
        val elapsed = measureTimeMillis {
            runBlocking {
                println("Weather Forecast")
                val forecast: Deferred<String> = async {
                    getForecast(duration = 5_000)
                }
                val temperature: Deferred<String> = async {
                    getTemperature(duration = 3_000)
                }
                println("${ forecast.await() } - ${ temperature.await()}")
                println("have a good day")
            }
        }
        println("Execution took $elapsed ms (${ elapsed / 1000 } seconds)")
        println("End of main")
    }

    @Test
    fun testGrouped() {
        println("Start of main")
        val elapsed = measureTimeMillis {
            runBlocking {
                println("Weather Forecast")
                val result = getWeatherReport()
                println(result)
                println("have a good day")
            }
        }
        println("Execution took $elapsed ms (${ elapsed / 1000 } seconds)")
        println("End of main")
    }

    @Test
    fun testGrouped_Error() {
        println("Start of main")
        val elapsed = measureTimeMillis {
            runBlocking {
                println("Weather Forecast")
                try {
                    val forecast: Deferred<String> = async {
                        getForecast(duration = 5_000)
                    }
                    val temperature: Deferred<String> = async {
                        getTemperature(duration = 3_000)
                    }

                    delay(200)
                    temperature.cancel()

                    println("${ forecast.await() } - ${ if (temperature.isCompleted) temperature.await() else "none"}")
                } catch (e: AssertionError) {
                    println("Caught exception in runBlocking(): $e")
                    println("Report unavailable at this time")
                }
                println("have a good day")
            }
        }
        println("Execution took $elapsed ms (${ elapsed / 1000 } seconds)")
        println("End of main")
    }

    @Test
    fun context() {
        runBlocking {
            println("${Thread.currentThread().name} - runBlocking function")
            launch {
                println("${Thread.currentThread().name} - launch function")
                withContext(Dispatchers.Default) {
                    println("${Thread.currentThread().name} - withContext function")
                    delay(1000)
                    println("10 results found.")
                }
                println("${Thread.currentThread().name} - end of launch function")
            }
            println("Loading...")
        }
    }

}