import org.example.Calculator
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculatorTest {

    private val calculator = Calculator()

    @Test
    fun testAdd() {
        val result = calculator.add(3, 5)
        assertEquals(8, result)
    }

    @Test
    fun testSubtract() {
        val result = calculator.subtract(10, 4)
        assertEquals(6, result)
    }

}