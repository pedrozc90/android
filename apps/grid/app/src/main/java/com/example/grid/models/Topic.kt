package com.example.grid.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Topic(
    @StringRes val stringId: Int,
    @DrawableRes val imageId: Int,
    val count: Int
)
