package unit2

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HighOrderFunctionsTest {

    val cookies = listOf<Cookie>(
        Cookie("Chocolate Chip", false, false, 1.69),
        Cookie("Banana Walnut", true, false, 1.49),
        Cookie("Vanilla Creme", false, true, 1.59),
        Cookie("Chocolate Peanut Butter", false, true, 1.49),
        Cookie("Snickerdoodle", true, false, 1.39),
        Cookie("Blueberry Tart", true, true, 1.79),
        Cookie("Sugar and Sprinkles", false, false, 1.39)
    )

    @Test
    fun repeat() {
        repeat(cookies.size) {
            val element = cookies[it]
            assertNotNull(element.name)
            assertNotNull(element.price)
        }
    }

    @Test
    fun forLoop() {
        cookies.forEach({ element ->
            assertNotNull(element.name)
            assertNotNull(element.price)
        })

        cookies.forEach {
            println(it)
            println("Menu Item: $it.name") // outputs like "Menu item: Cookie@5a10411.name"
            println("Menu Item: ${it.name}") // outputs like "Menu item: Chocolate Chip"
        }
    }

    @Test
    fun map() {
        val fullMenu = cookies.map { "${it.name} - ${it.price}" }
        assertEquals(7, fullMenu.size)
        fullMenu.forEach { assertInstanceOf(String::class.java, it) }
    }

    @Test
    fun filter() {
        val softBakedMenu = cookies.filter { it.softBaked }
            .map { "${it.name} - ${it.price}" }
        assertEquals(3, softBakedMenu.size)
        softBakedMenu.forEach { assertInstanceOf(String::class.java, it) }
    }

    @Test
    fun groupBy() {
        val groupedMenu = cookies.groupBy { it.softBaked }
        assertEquals(2, groupedMenu.size)

        val softBakedMenu = groupedMenu[true] ?: listOf<Cookie>()
        assertEquals(3, softBakedMenu.size)

        for (element in softBakedMenu) {
            assertAll(
                { assertNotNull(element.name) },
                { assertNotNull(element.price) },
                { assertTrue(element.softBaked) }
            )
        }

        val crunchyMenu = groupedMenu[false]
        assertEquals(4, crunchyMenu?.size)

        if (crunchyMenu != null) {
            for (element in crunchyMenu) {
                assertAll(
                    { assertNotNull(element.name) },
                    { assertNotNull(element.price) },
                    { assertFalse(element.softBaked) }
                )
            }
        }
    }

    @Test
    fun fold() {
        val totalPrice = cookies.fold(0.00) { total, cookie -> total + cookie.price }
        assertEquals(10.83, totalPrice, 0.01)

        val reducedPrice = cookies.map { cookie -> cookie.price }
            .reduce { first, price -> first + price }
        assertEquals(10.83, reducedPrice, 0.01)

        val reducedCookie = cookies.reduce { first, price ->
            first.copy(name = "Reduced Cookie", price = first.price + price.price)
        }
        assertAll(
            { assertEquals("Reduced Cookie", reducedCookie.name) },
            { assertEquals(10.83, reducedCookie.price, 0.01) }
        )

        val sumOfPrice = cookies.sumOf { it.price }
        assertEquals(10.83, sumOfPrice, 0.01)

        val sumPrice = cookies.map { cookie -> cookie.price }.sum()
        assertEquals(10.83, sumPrice, 0.01)
    }

    @Test
    fun sort() {
        val sortedNames = cookies.map { cookie -> cookie.name }.sorted()
        assertEquals(7, sortedNames.size)
        assertEquals("Banana Walnut", sortedNames[0])
        assertEquals("Vanilla Creme", sortedNames[6])

        val sortedCookies = cookies.sortedBy { it.name }
        assertEquals(7, sortedCookies.size)
        assertEquals("Banana Walnut", sortedCookies[0].name)
        assertEquals("Vanilla Creme", sortedCookies[6].name)
    }

    data class Cookie(
        val name: String,
        val softBaked: Boolean,
        val hasFilling: Boolean,
        val price: Double
    )

}