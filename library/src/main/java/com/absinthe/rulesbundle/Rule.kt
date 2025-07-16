package com.absinthe.rulesbundle

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class Rule(
    val libName: String,
    @LibType val libType: Int,
    val label: String,
    @DrawableRes val iconRes: Int,
    val descriptionUrl: String?,
    val regexName: String?,
    val isSimpleColorIcon: Boolean
) : Parcelable