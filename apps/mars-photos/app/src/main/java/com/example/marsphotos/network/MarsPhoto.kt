package com.example.marsphotos.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarsPhoto(
    @SerialName(value = "id") val id: String,
    @SerialName(value = "img_src") val imgSrc: String
)
