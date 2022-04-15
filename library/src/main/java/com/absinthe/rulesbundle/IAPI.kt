package com.absinthe.rulesbundle

interface IAPI {
    fun getVersion(): Int
    fun getItemCounts(): Int
    fun getRulesAssetPath(): String
    suspend fun getRule(libName: String, @LibType type: Int, useRegex: Boolean): Rule?
    fun setLocale(locale: LCLocale)
    fun setRemoteRepo(repo: LCRemoteRepo)
}