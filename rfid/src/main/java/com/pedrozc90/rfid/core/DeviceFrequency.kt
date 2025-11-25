package com.pedrozc90.rfid.core

enum class DeviceFrequency(val label: String) {

    CHINA_1(label = "China (840~845 MHz)"),
    CHINA_2(label = "China (920~925 MHz)"),
    EUROPE(label = "Europe (865~868 MHz)"),
    UNITED_STATES(label = "United States (902~928 MHz)"),
    KOREAN(label = "Korea (917~923 MHz)"),
    JAPAN(label = "Japan (916.8~920.8 MHz)"),
    SOUTH_AFRICA(label = "South Africa (915~919 MHz)"),
    TAIWAN(label = "Taiwan (920~928 MHz)"),
    VIETNAM(label = "Vietnam (918~923 MHz)"),
    PERU(label = "Peru (915~928 MHz)"),
    RUSSIA(label = "Russia (860~867.6 MHz)"),
    MOROCCO(label = "Morocco (914~921 MHz)"),
    MALAYSIA(label = "Malaysia (919~923 MHz)"),
    BRAZIL(label = "Brazil (902~907.5 MHz)"),
    BRAZIL_LOWER(label = "Brazil (902~907.5 MHz & 915~928 MHz)"),
    BRAZIL_UPPER(label = "Brazil (915~928 MHz)");

    companion object {

        private val _map = entries.associateBy { it.name }

        val options = entries.sortedBy { it.label }

        fun of(name: String?, default: DeviceFrequency = BRAZIL): DeviceFrequency {
            return name.let { _map[it] } ?: default
        }

    }

}
