package com.absinthe.rulesbundle

import android.provider.BaseColumns

data class RuleEntity(
  val id: Int,
  val name: String,
  val label: String,
  @LibType val type: Int,
  val iconIndex: Int,
  val isRegexRule: Boolean,
  val regexName: String?
)
