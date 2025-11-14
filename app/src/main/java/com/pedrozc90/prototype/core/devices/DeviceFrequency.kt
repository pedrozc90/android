package com.pedrozc90.prototype.core.devices

enum class DeviceFrequency(
    val key: String,
    val label: String
) {
    CHINA_1(key = "china_1", label = "China Standard 1 (840~845 MHz)"),
    CHINA_2(key = "china_2", label = "China Standard 2 (920~925 MHz)"),
    EUROPE(key = "europe", label = "Europe Standard (865~868 MHz)"),
    UNITED_STATES(key = "united_states", label = "United States Standard (902~928 MHz)"),
    KOREAN(key = "korea", label = "Korea (917~923 MHz)"),
    JAPAN(key = "japan", label = "Japan (916.8~920.8 MHz)"),
    SOUTH_AFRICA(key = "south_africa", label = "South Africa (915~919 MHz)"),
    TAIWAN(key = "taiwan", label = "Taiwan (920~928 MHz)"),
    VIETNAM(key = "vietnam", label = "Vietnam (918~923 MHz)"),
    PERU(key = "peru", label = "Peru (915~928 MHz)"),
    RUSSIA(key = "russia", label = "Russia (860~867.6 MHz)"),
    MOROCCO(key = "morocco", label = "Morocco (914~921 MHz)"),
    MALAYSIA(key = "malaysia", label = "Malaysia (919~923 MHz)"),
    BRAZIL(key = "brazil", label = "Brazil (902~907.5 MHz)"),
    BRAZIL_LOWER(key = "brazil_1", label = "Brazil (902~907.5 MHz & 915~928 MHz)"),
    BRAZIL_UPPER(key = "brazil_2", label = "Brazil (915~928 MHz)");

    // abstract fun toChainway(): Int
    // abstract fun toUrovo(): Int
    // abstract fun toBluebird(): Int

    companion object {

        private val _map = entries.associateBy { it.name }

        fun fromName(name: String?, default: DeviceFrequency = BRAZIL): DeviceFrequency {
            return name.let { _map[it] } ?: default
        }

    }

}
