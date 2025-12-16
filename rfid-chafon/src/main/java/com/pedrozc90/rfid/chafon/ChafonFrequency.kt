package com.pedrozc90.rfid.chafon

import com.pedrozc90.rfid.core.DeviceFrequency

sealed class ChafonFrequency(
    val band: Int,          // region band mask
    val fStart: Float,      // initial frequency in MHz
    val fStep: Float,       // frequency step in MHz
    val minIndex: Int = 0,  // minimum channel index
    val maxIndex: Int = 63  // maximum channel index
) {
    object All : ChafonFrequency(band = 0, fStart = 840.0f, fStep = 2.00f, minIndex = 0, maxIndex = 60)               // 840.00 ~ 960.00 MHz
    object ChineseUpper : ChafonFrequency(band = 1, fStart = 920.125f, fStep = 0.25f, minIndex = 0, maxIndex = 19)    // 920.125 ~ 924.875 MHz
    object UnitedStates : ChafonFrequency(band = 2, fStart = 902.75f, fStep = 0.50f, minIndex = 0, maxIndex = 49)     // 902.75 ~ 927.25 MHz
    object Korean : ChafonFrequency(band = 3, fStart = 917.10f, fStep = 0.20f, minIndex = 0, maxIndex = 31)           // 917.10 ~ 923.30 MHz
    object EU_LOWER : ChafonFrequency(band = 4, fStart = 865.10f, fStep = 0.20f, minIndex = 0, maxIndex = 14)         // 865.10 ~ 867.90 MHz
    object Ukraine : ChafonFrequency(band = 6, fStart = 868.00f, fStep = 0.10f, minIndex = 0, maxIndex = 6)           // 868.00 ~ 868.60 MHz
    object ChineseLower : ChafonFrequency(band = 8, fStart = 840.125f, fStep = 0.25f, minIndex = 0, maxIndex = 19)    // 840.125 ~ 844.875 MHz
    object EU_3 : ChafonFrequency(band = 9, fStart = 865.70f, fStep = 0.60f, minIndex = 0, maxIndex = 3)              // 865.70 ~ 867.50 MHz
    object US_3 : ChafonFrequency(band = 12, fStart = 902.00f, fStep = 0.50f, minIndex = 0, maxIndex = 52)            // 902.00 ~ 928.00 MHz
    object HongKong : ChafonFrequency(band = 16, fStart = 920.25f, fStep = 0.50f, minIndex = 0, maxIndex = 9)         // 920.25 ~ 924.75 MHz
    object Taiwan : ChafonFrequency(band = 17, fStart = 920.75f, fStep = 0.50f, minIndex = 0, maxIndex = 13)          // 920.75 ~ 927.25 MHz
    object ETSI_UPPER : ChafonFrequency(band = 18, fStart = 916.30f, fStep = 1.20f, minIndex = 0, maxIndex = 2)       // 916.30 ~ 918.70 MHz (ETSI = European Telecommunications Standards Institute)
    object Malaysia : ChafonFrequency(band = 19, fStart = 919.25f, fStep = 0.50f, minIndex = 0, maxIndex = 7)         // 919.25 ~ 922.75 MHz
    object Brazil : ChafonFrequency(band = 21, fStart = 902.75f, fStep = 0.50f, minIndex = 0, maxIndex = 34)          // 0 - 9 = 902.75 ~ 907.25 MHz; 10 - 34 910.25 ~ 927.25 MHz
    object Thailand : ChafonFrequency(band = 22, fStart = 920.25f, fStep = 0.50f, minIndex = 0, maxIndex = 9)         // 920.25 ~ 924.75 MHz
    object Singapore : ChafonFrequency(band = 23, fStart = 920.25f, fStep = 0.50f, minIndex = 0, maxIndex = 9)        // 920.25 ~ 924.75 MHz
    object Australia : ChafonFrequency(band = 24, fStart = 920.25f, fStep = 0.50f, minIndex = 0, maxIndex = 9)        // 920.25 ~ 924.75 MHz
    object India : ChafonFrequency(band = 25, fStart = 865.10f, fStep = 0.60f, minIndex = 0, maxIndex = 3)            // 865.10 ~ 866.90 MHz
    object Uruguay : ChafonFrequency(band = 26, fStart = 916.25f, fStep = 0.50f, minIndex = 0, maxIndex = 22)         // 916.25 ~ 927.25 MHz
    object Vietnam : ChafonFrequency(band = 27, fStart = 918.75f, fStep = 0.50f, minIndex = 0, maxIndex = 7)          // 918.75 ~ 922.25 MHz
    object Israel : ChafonFrequency(band = 28, fStart = 916.25f, fStep = 0.50f, minIndex = 0, maxIndex = 0)           // 916.25 ~ 916.25 MHz
    object Indonesia : ChafonFrequency(band = 29, fStart = 917.25f, fStep = 0.50f, minIndex = 0, maxIndex = 3)        // 0 = 917.25 ~ 917.25 MHz; 0 - 3 = 920.25 ~ 921.75 MHz
    object NewZealand : ChafonFrequency(band = 30, fStart = 922.25f, fStep = 0.50f, minIndex = 0, maxIndex = 9)       // 922.25 ~ 926.75 MHz
    object Japan2 : ChafonFrequency(band = 31, fStart = 916.80f, fStep = 1.20f, minIndex = 0, maxIndex = 3)           // 916.80 ~ 920.40 MHz
    object Peru : ChafonFrequency(band = 32, fStart = 916.25f, fStep = 0.50f, minIndex = 0, maxIndex = 22)            // 916.25 ~ 927.25 MHz
    object Russia : ChafonFrequency(band = 33, fStart = 916.20f, fStep = 1.20f, minIndex = 0, maxIndex = 3)           // 916.20 ~ 919.80 MHz
    object SouthAfrica : ChafonFrequency(band = 34, fStart = 915.60f, fStep = 0.20f, minIndex = 0, maxIndex = 16)     // 915.60 ~ 918.80 MHz
    object Philippines : ChafonFrequency(band = 35, fStart = 918.25f, fStep = 0.50f, minIndex = 0, maxIndex = 3)      // 918.25 ~ 919.75 MHz

    /**
     * Calculate device Fmin: the minimum frequency point (channel at minIndex)
     */
    val minFrequency: Float
        get() = fStart + (fStep * minIndex)

    val maxFrequency: Float
        get() = fStart + (fStep * maxIndex)

    fun toDeviceFrequency(): DeviceFrequency? {
        return when (this) {
            ChineseLower -> DeviceFrequency.CHINA_1
            ChineseUpper -> DeviceFrequency.CHINA_2
            EU_LOWER -> DeviceFrequency.EUROPE
            UnitedStates -> DeviceFrequency.UNITED_STATES
            Korean -> DeviceFrequency.KOREAN
            Japan2 -> DeviceFrequency.JAPAN
            SouthAfrica -> DeviceFrequency.SOUTH_AFRICA
            Taiwan -> DeviceFrequency.TAIWAN
            Vietnam -> DeviceFrequency.VIETNAM
            Peru -> DeviceFrequency.PERU
            Russia -> DeviceFrequency.RUSSIA
            All -> DeviceFrequency.MOROCCO
            Malaysia -> DeviceFrequency.MALAYSIA
            Brazil -> DeviceFrequency.BRAZIL
            // Brazil -> DeviceFrequency.BRAZIL_LOWER
            // Brazil -> DeviceFrequency.BRAZIL_UPPER
            else -> null
        }
    }

    companion object {

        private val _values by lazy {
            listOf(
                All,
                ChineseUpper,
                UnitedStates,
                Korean,
                EU_LOWER,
                Ukraine,
                ChineseLower,
                EU_3,
                US_3,
                HongKong,
                Taiwan,
                ETSI_UPPER,
                Malaysia,
                Brazil,
                Thailand,
                Singapore,
                Australia,
                India,
                Uruguay,
                Vietnam,
                Israel,
                Indonesia,
                NewZealand,
                Japan2,
                Peru,
                Russia,
                SouthAfrica,
                Philippines,
            )
        }

        private val _masks by lazy { _values.associateBy { it.band } }

        fun of(value: Int?): ChafonFrequency? {
            return value.let { _masks[it] }
        }

    }

}

fun DeviceFrequency.toChafon(): ChafonFrequency? {
    return when (this) {
        DeviceFrequency.CHINA_1 -> ChafonFrequency.ChineseLower
        DeviceFrequency.CHINA_2 -> ChafonFrequency.ChineseUpper
        DeviceFrequency.EUROPE -> ChafonFrequency.EU_LOWER
        DeviceFrequency.UNITED_STATES -> ChafonFrequency.UnitedStates
        DeviceFrequency.KOREAN -> ChafonFrequency.Korean
        DeviceFrequency.JAPAN -> ChafonFrequency.Japan2
        DeviceFrequency.SOUTH_AFRICA -> ChafonFrequency.SouthAfrica
        DeviceFrequency.TAIWAN -> ChafonFrequency.Taiwan
        DeviceFrequency.VIETNAM -> ChafonFrequency.Vietnam
        DeviceFrequency.PERU -> ChafonFrequency.Peru
        DeviceFrequency.RUSSIA -> ChafonFrequency.Russia
        DeviceFrequency.MOROCCO -> ChafonFrequency.All
        DeviceFrequency.MALAYSIA -> ChafonFrequency.Malaysia
        DeviceFrequency.BRAZIL -> ChafonFrequency.Brazil
        DeviceFrequency.BRAZIL_LOWER -> ChafonFrequency.Brazil
        DeviceFrequency.BRAZIL_UPPER -> ChafonFrequency.Brazil
        else -> null
    }
}
