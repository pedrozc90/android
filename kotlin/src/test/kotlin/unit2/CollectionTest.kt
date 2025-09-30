package unit2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CollectionTest {

    @Test
    fun test_1() {
        val rockPlanets = arrayOf<String>("Mercury", "Venus", "Earth", "Mars")
        val gasPlanets = arrayOf("Jupiter", "Saturn", "Uranus", "Neptune")
        val solarSystem = rockPlanets + gasPlanets
        assertEquals(solarSystem.size, 8)
        assertEquals("Mercury", solarSystem[0])
        assertEquals("Venus", solarSystem[1])
        assertEquals("Earth", solarSystem[2])
        assertEquals("Mars", solarSystem[3])
        assertEquals("Jupiter", solarSystem[4])
        assertEquals("Saturn", solarSystem[5])
        assertEquals("Uranus", solarSystem[6])
        assertEquals("Neptune", solarSystem[7])

        val result = assertThrows(ArrayIndexOutOfBoundsException::class.java) { solarSystem[8] = "Pluto" }
        assertEquals("Index 8 out of bounds for length 8", result.message)

        val newSolarSystem = solarSystem.copyOf(solarSystem.size + 1)
        newSolarSystem[8] = "Pluto"
    }

    @Test
    fun test_2() {
        val solarSystem = listOf<String>("Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune")
        assertEquals(solarSystem.size, 8)
        assertEquals("Mercury", solarSystem.get(0))
        assertEquals("Venus", solarSystem.get(1))
        assertEquals("Earth", solarSystem.get(2))
        assertEquals("Mars", solarSystem.get(3))
        assertEquals("Jupiter", solarSystem.get(4))
        assertEquals("Saturn", solarSystem.get(5))
        assertEquals("Uranus", solarSystem.get(6))
        assertEquals("Neptune", solarSystem.get(7))

        val earthIndex = solarSystem.indexOf("Earth")
        assertEquals(2, earthIndex)

        val plutoIndex = solarSystem.indexOf("Pluto")
        assertEquals(-1, plutoIndex)

        // not available
        // solarSystem[0] = "Theia"
        // solarSystem.add("Theia")
        // solarSystem.removeAt(7)

        assertFalse { solarSystem.contains("Pluto") }
        assertFalse { "Pluto" in solarSystem }

        for (planet in solarSystem) {
            println(planet)
        }

        val newSolarSystem = mutableListOf<String>("Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune")
        newSolarSystem.add("Pluto")
        newSolarSystem.add("Theia")
    }

    @Test
    fun test_3() {
        val hashCode = "Kotlin".hashCode()
        assertEquals(-2041707231,  hashCode)

        val solarSystem = mutableSetOf<String>("Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto")
        assertEquals(9, solarSystem.size)
        assertTrue(solarSystem.contains("Earth"))

        solarSystem.add("Theia")
        assertEquals(10, solarSystem.size)
        assertTrue(solarSystem.contains("Theia"))

        solarSystem.remove("Theia")
        assertEquals(9, solarSystem.size)
    }

    @Test
    fun test_4() {
        val solarSystem = mutableMapOf<String, Int>(
            "Mercury" to 0,
            "Venus" to 0,
            "Earth" to 1,
            "Mars" to 2,
            "Jupiter" to 79,
            "Saturn" to 82,
            "Uranus" to 27,
            "Neptune" to 14,
        )

        assertEquals(8, solarSystem.size)
        assertEquals(1, solarSystem["Earth"])
    }

}