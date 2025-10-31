package com.pedrozc90.rfid.core

fun Int.clamp(min: Int, max: Int): Int = coerceIn(min, max)
