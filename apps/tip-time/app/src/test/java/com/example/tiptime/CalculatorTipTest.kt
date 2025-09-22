package com.example.tiptime

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.NumberFormat

class CalculatorTipTest {

    @Test
    fun calculateTip_20PercentNoRoundUp() {
        val amount = 100.00
        val percent = 20.00
        val expectedTip = NumberFormat.getCurrencyInstance().format(20)
        val actualTip = calculateTip(amount, percent, false)
        assertEquals(expectedTip, actualTip)
    }

    @Test
    fun calculateTip_20PercentRoundUp() {
        val amount = 100.30
        val percent = 15.00
        val expectedTip = NumberFormat.getCurrencyInstance().format(16)
        val actualTip = calculateTip(amount, percent, true)
        assertEquals(expectedTip, actualTip)
    }

}