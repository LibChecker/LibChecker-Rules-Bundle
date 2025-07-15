package com.absinthe.rulesbundle

import android.content.Context
import java.lang.ref.WeakReference
import java.util.*

object LCRules : IAPI {

    private var contextRef: WeakReference<Context>? = null
    private var locale: LCLocale = LCLocale.ZH
    private var repo: LCRemoteRepo = LCRemoteRepo.Github
    private var baseUrl: String = Urls.GITHUB_ROOT_URL

    private var ruleRepo: RuleRepository? = null

    fun init(context: Context) {
        Repositories.checkRulesDatabase(context)
        contextRef = WeakReference(context.applicationContext)
        ruleRepo = RuleRepository(context.applicationContext)
    }

    fun closeDb() {
        ruleRepo?.closeDb()
    }

    override fun getVersion(): Int {
        contextRef?.get()?.assets?.open("lcrules/version.prop")?.let {
            runCatching {
                Properties().apply {
                    load(it)
                    return getProperty("version").toInt()
                }
            }.onFailure {
                return 0
            }
        }
        return 0
    }

    override fun getItemCounts(): Int {
        contextRef?.get()?.assets?.open("lcrules/version.prop")?.let {
            runCatching {
                Properties().apply {
                    load(it)
                    return getProperty("items").toInt()
                }
            }.onFailure {
                return 0
            }
        }
        return 0
    }

    override fun getRulesAssetPath(): String = "lcrules/rules.db"

    override suspend fun getRule(libName: String, @LibType type: Int, useRegex: Boolean): Rule? {
        val repo = ruleRepo ?: return null
        val entity = repo.getRule(libName, type)
        if (entity != null) {
            return Rule(
                entity.label,
                IconResMap.getIconRes(entity.iconIndex),
                getDescriptionUrl(entity),
                entity.regexName,
                IconResMap.isSingleColorIcon(entity.iconIndex)
            )
        }
        if (useRegex) {
            val regexMap = repo.getRegexRules()
            val match = regexMap.entries.firstOrNull { it.key.matcher(libName).matches() && it.value.type == type }
            match?.value?.let {
                return Rule(
                    it.label,
                    IconResMap.getIconRes(it.iconIndex),
                    getDescriptionUrl(it),
                    it.regexName,
                    IconResMap.isSingleColorIcon(it.iconIndex)
                )
            }
        }
        return null
    }

    override fun setLocale(locale: LCLocale) {
        this.locale = locale
    }

    override fun setRemoteRepo(repo: LCRemoteRepo) {
        this.repo = repo
        this.baseUrl = when (repo) {
            LCRemoteRepo.Github -> Urls.GITHUB_ROOT_URL
            LCRemoteRepo.Gitlab -> Urls.GITLAB_ROOT_URL
        }
    }

    private val dirMap = mapOf(
        NATIVE to "native-libs",
        SERVICE to "services-libs",
        ACTIVITY to "activities-libs",
        RECEIVER to "receivers-libs",
        PROVIDER to "providers-libs",
        DEX to "dex-libs",
        STATIC to "static-libs",
    )

    private fun getDescriptionUrl(entity: RuleEntity): String? {
        val dir = dirMap[entity.type] ?: return null
        return "$baseUrl$dir/${entity.name}.json"
    }
}