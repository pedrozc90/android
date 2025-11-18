package com.pedrozc90.rfid.core

import kotlin.collections.get

enum class DeviceFrequency(
    val key: String,
    val label: String,
    val chainway: Int? = null
) {
    CHINA_1(key = "china_1", label = "China (840~845 MHz)", chainway = 0x01),
    CHINA_2(key = "china_2", label = "China (920~925 MHz)", chainway = 0x02),
    EUROPE(key = "europe", label = "Europe (865~868 MHz)", chainway = 0x04),
    UNITED_STATES(key = "united_states", label = "United States (902~928 MHz)", chainway = 0x08),
    KOREAN(key = "korea", label = "Korea (917~923 MHz)", chainway = 0x16),
    JAPAN(key = "japan", label = "Japan (916.8~920.8 MHz)", chainway = 0x32),
    SOUTH_AFRICA(key = "south_africa", label = "South Africa (915~919 MHz)", chainway = 0x33),
    TAIWAN(key = "taiwan", label = "Taiwan (920~928 MHz)", chainway = 0x34),
    VIETNAM(key = "vietnam", label = "Vietnam (918~923 MHz)", chainway = 0x35),
    PERU(key = "peru", label = "Peru (915~928 MHz)", chainway = 0x36),
    RUSSIA(key = "russia", label = "Russia (860~867.6 MHz)", chainway = 0x37),
    MOROCCO(key = "morocco", label = "Morocco (914~921 MHz)", chainway = 0x80),
    MALAYSIA(key = "malaysia", label = "Malaysia (919~923 MHz)", chainway = 0x3B),
    BRAZIL(key = "brazil", label = "Brazil (902~907.5 MHz)", chainway = 0x3C),
    BRAZIL_LOWER(key = "brazil_1", label = "Brazil (902~907.5 MHz & 915~928 MHz)", chainway = 0x3C),
    BRAZIL_UPPER(key = "brazil_2", label = "Brazil (915~928 MHz)", chainway = 0x36);

    // TODO: how do we translate the enum into device-specific frequency settings?

    companion object {

        private val _map = entries.associateBy { it.name }

        val options = entries.sortedBy { it.label }

        fun of(name: String?, default: DeviceFrequency = BRAZIL): DeviceFrequency {
            return name.let { _map[it] } ?: default
        }

    }

}
