package com.absinthe.rulesbundle

import android.content.Context
import java.util.*

object LCRules {

    private const val RULES_ASSET_PATH = "lcrules/rules.db"
    private const val VERSION_ASSET_PATH = "lcrules/version.prop"

    private var locale: LCLocale = LCLocale.ZH
    private var remoteRepo: LCRemoteRepo = LCRemoteRepo.GitHub
    private var metadata = RuleMetadata()

    private var ruleStore: RuleStore? = null

    @Synchronized
    fun init(context: Context) {
        val applicationContext = context.applicationContext
        metadata = readMetadata(applicationContext)
        ruleStore?.close()
        ruleStore = RuleStore.open(applicationContext, RULES_ASSET_PATH, metadata.version)
    }

    fun close() {
        ruleStore?.close()
        ruleStore = null
    }

    @Deprecated("Use close().", ReplaceWith("close()"))
    fun closeDb() {
        close()
    }

    fun getVersion(): Int = metadata.version

    fun getItemCounts(): Int = metadata.items

    fun getRulesAssetPath(): String = RULES_ASSET_PATH

    suspend fun getRule(libName: String, @LibType type: Int, useRegex: Boolean): Rule? {
        val record = ruleStore?.findRule(libName, type, useRegex) ?: return null
        return record.toRule(libName)
    }

    fun setLocale(locale: LCLocale) {
        this.locale = locale
    }

    fun setRemoteRepo(repo: LCRemoteRepo) {
        this.remoteRepo = repo
    }

    private val dirMap = mapOf(
        NATIVE to "native-libs",
        SERVICE to "services-libs",
        ACTIVITY to "activities-libs",
        RECEIVER to "receivers-libs",
        PROVIDER to "providers-libs",
        DEX to "dex-libs",
        STATIC to "static-libs",
        ACTION to "actions-libs",
    )

    private fun RuleRecord.toRule(libName: String): Rule {
        return Rule(
            libName = libName,
            libType = type,
            label = label,
            iconRes = IconResMap.getIconRes(iconIndex),
            descriptionUrl = getDescriptionUrl(this),
            regexName = regexName,
            isSimpleColorIcon = IconResMap.isSingleColorIcon(iconIndex)
        )
    }

    private fun getDescriptionUrl(record: RuleRecord): String? {
        val dir = dirMap[record.type] ?: return null
        val fileName = record.regexName ?: record.name
        return "${remoteRepo.rootUrl}$dir/$fileName.json"
    }

    private fun readMetadata(context: Context): RuleMetadata {
        return runCatching {
            context.assets.open(VERSION_ASSET_PATH).use {
                Properties().apply { load(it) }
            }
        }.map { properties ->
            RuleMetadata(
                version = properties.getProperty("version")?.toIntOrNull() ?: 0,
                items = properties.getProperty("items")?.toIntOrNull() ?: 0
            )
        }.getOrDefault(RuleMetadata())
    }

    private data class RuleMetadata(
        val version: Int = 0,
        val items: Int = 0
    )
}
