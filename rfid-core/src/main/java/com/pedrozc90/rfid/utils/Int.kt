package com.pedrozc90.rfid.utils

fun Int.clamp(min: Int, max: Int): Int = coerceIn(min, max)
