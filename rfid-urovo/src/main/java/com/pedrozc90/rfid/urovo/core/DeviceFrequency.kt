package com.pedrozc90.rfid.urovo.core

import com.pedrozc90.rfid.core.DeviceFrequency

sealed class UrovoFrequency(val ref: DeviceFrequency?) {

    // typical Urovo representation for most regions: region, start, end channel indexes
    open class Range(val region: Int, val start: Int, val end: Int, ref: DeviceFrequency?) :
        UrovoFrequency(ref) {
        override fun toString(): String = "Range(bind=$region, min=$start, max=$end)"
    }

    // alternate representation (flags / band / spacing) used by some devices/regions
    open class Band(
        val flags: Int,
        val band: Int,
        val space: Int,
        val num: Int,
        val start: Int,
        ref: DeviceFrequency?
    ) : UrovoFrequency(ref) {
        override fun toString(): String =
            "Band(flags=$flags, band=$band, space=$space, num=$num, start=$start)"
    }

    // TODO: populate predefined frequencies instances
    object China : Range(region = 0, start = 0, end = 63, ref = DeviceFrequency.CHINA_1)
    object Brazil : Band(flags = 0, band = 0, space = 0, num = 0, start = 0, ref = DeviceFrequency.BRAZIL)

    /**
     * Reverse mapping from a UrovoFrequency to the core DeviceFrequency where possible.
     * Returns null when no mapping is recognized.
     */
    fun map(): DeviceFrequency? {
        return when (this) {
            is Range -> {
                val freq = of(region = region)
                return freq?.ref
            }

            is Band -> {
                // Try to recognize the Brazil Band signature you had in your example:
                if (flags == 1 && band == 0 && space == 30 && num == 0 && start == 10) {
                    DeviceFrequency.BRAZIL
                } else {
                    null
                }
            }
        }
    }

    companion object {

        fun of(region: Int): Range? {
            return when (region) {
                0 -> China
                else -> null
            }
        }

        fun of(region: Byte, start: Byte, end: Byte): Range? {
            return of(region = region.toInt())
        }

    }
}

/**
 * Convert core DeviceFrequency to Urovo-specific parameter object.
 * Returns null when there is no sensible mapping.
 */
fun DeviceFrequency.toUrovo(): UrovoFrequency? = when (this) {
    DeviceFrequency.CHINA_1 -> UrovoFrequency.China
    DeviceFrequency.BRAZIL_LOWER -> UrovoFrequency.Brazil
    else -> null
}
