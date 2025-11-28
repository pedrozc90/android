package com.pedrozc90.rfid.chainway

import com.pedrozc90.rfid.core.DeviceFrequency
import kotlin.collections.get

sealed class ChainwayFrequency(val mask: Int) {

    object ChinaLower : ChainwayFrequency(mask = 0x01) // China (840~845 MHz)
    object ChinaUpper : ChainwayFrequency(mask = 0x02) // China (920~925 MHz)
    object Europe : ChainwayFrequency(mask = 0x04) // Europe (865~868 MHz)
    object UnitedStates : ChainwayFrequency(mask = 0x08) // United States (902~928 MHz)
    object Korean : ChainwayFrequency(mask = 0x16) // Korea (917~923 MHz)
    object Japan : ChainwayFrequency(mask = 0x32) // Japan (916.8~920.8 MHz)
    object SouthAfrica : ChainwayFrequency(mask = 0x33) // South Africa (915~919 MHz)
    object Taiwan : ChainwayFrequency(mask = 0x34) // Taiwan (920~928 MHz)
    object Vietnam : ChainwayFrequency(mask = 0x35) // Vietnam (918~923 MHz)
    object Peru : ChainwayFrequency(mask = 0x36) // Peru (915~928 MHz)
    object Russia : ChainwayFrequency(mask = 0x37) // Russia (860~867.6 MHz)
    object Morocco : ChainwayFrequency(mask = 0x80) // Morocco (914~921 MHz)
    object Malaysia : ChainwayFrequency(mask = 0x3B) // Malaysia (919~923 MHz)
    object Brazil : ChainwayFrequency(mask = 0x3C) // Brazil (902~907.5 MHz)
    object BrazilLower : ChainwayFrequency(mask = 0x3C) // Brazil (902~907.5 MHz & 915~928 MHz)
    object BrazilUpper : ChainwayFrequency(mask = 0x36) // Brazil (915~928 MHz)

    fun toDeviceFrequency(): DeviceFrequency? {
        return when (this) {
            ChinaLower -> DeviceFrequency.CHINA_1
            ChinaUpper -> DeviceFrequency.CHINA_2
            Europe -> DeviceFrequency.EUROPE
            UnitedStates -> DeviceFrequency.UNITED_STATES
            Korean -> DeviceFrequency.KOREAN
            Japan -> DeviceFrequency.JAPAN
            SouthAfrica -> DeviceFrequency.SOUTH_AFRICA
            Taiwan -> DeviceFrequency.TAIWAN
            Vietnam -> DeviceFrequency.VIETNAM
            Peru -> DeviceFrequency.PERU
            Russia -> DeviceFrequency.RUSSIA
            Morocco -> DeviceFrequency.MOROCCO
            Malaysia -> DeviceFrequency.MALAYSIA
            Brazil -> DeviceFrequency.BRAZIL
            BrazilLower -> DeviceFrequency.BRAZIL_LOWER
            BrazilUpper -> DeviceFrequency.BRAZIL_UPPER
        }
    }

    companion object {

        private val _values by lazy {
            listOf(
                ChinaLower,
                ChinaUpper,
                Europe,
                UnitedStates,
                Korean,
                Japan,
                SouthAfrica,
                Taiwan,
                Vietnam,
                Peru,
                Russia,
                Morocco,
                Malaysia,
                Brazil,
                BrazilLower,
                BrazilUpper
            )
        }

        private val _masks by lazy { _values.associateBy { it.mask } }

        fun of(value: Int?): ChainwayFrequency? {
            return value.let { _masks[it] }
        }

    }

}

fun DeviceFrequency.toChainway(): ChainwayFrequency? {
    return when (this) {
        DeviceFrequency.CHINA_1 -> ChainwayFrequency.ChinaLower
        DeviceFrequency.CHINA_2 -> ChainwayFrequency.ChinaUpper
        DeviceFrequency.EUROPE -> ChainwayFrequency.Europe
        DeviceFrequency.UNITED_STATES -> ChainwayFrequency.UnitedStates
        DeviceFrequency.KOREAN -> ChainwayFrequency.Korean
        DeviceFrequency.JAPAN -> ChainwayFrequency.Japan
        DeviceFrequency.SOUTH_AFRICA -> ChainwayFrequency.SouthAfrica
        DeviceFrequency.TAIWAN -> ChainwayFrequency.Taiwan
        DeviceFrequency.VIETNAM -> ChainwayFrequency.Vietnam
        DeviceFrequency.PERU -> ChainwayFrequency.Peru
        DeviceFrequency.RUSSIA -> ChainwayFrequency.Russia
        DeviceFrequency.MOROCCO -> ChainwayFrequency.Morocco
        DeviceFrequency.MALAYSIA -> ChainwayFrequency.Malaysia
        DeviceFrequency.BRAZIL -> ChainwayFrequency.Brazil
        DeviceFrequency.BRAZIL_LOWER -> ChainwayFrequency.BrazilLower
        DeviceFrequency.BRAZIL_UPPER -> ChainwayFrequency.BrazilUpper
    }
}
