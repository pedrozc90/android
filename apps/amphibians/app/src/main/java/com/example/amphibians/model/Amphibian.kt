package com.example.amphibians.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Data class that defines an amphibian which includes a name, type, description, and image URL.
 */
@Serializable
data class Amphibian(
    @SerialName(value = "name") val name: String,
    @SerialName(value = "type") val type: String,
    @SerialName(value = "description") val description: String,
    @SerialName(value = "img_src") val imgSrc: String
)
