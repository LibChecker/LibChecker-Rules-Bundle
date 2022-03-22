package com.absinthe.rulesbundle

import androidx.annotation.DrawableRes

data class Rule(
    val label: String,
    @DrawableRes val iconRes: Int,
    val descriptionUrl: String?,
    val isSimpleColorIcon: Boolean
)