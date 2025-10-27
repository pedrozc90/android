package com.pedrozc90.prototype.data.web.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName(value = "username")
    val username: String,

    @SerialName(value = "password")
    val password: String
)
