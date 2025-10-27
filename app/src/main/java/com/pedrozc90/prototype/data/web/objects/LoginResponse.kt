package com.pedrozc90.prototype.data.web.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName(value = "token")
    val token: String,

    @SerialName(value = "expires_at")
    val expiresAt: String
)
