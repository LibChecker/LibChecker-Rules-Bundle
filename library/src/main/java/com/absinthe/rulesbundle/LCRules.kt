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
        contextRef = WeakReference(context)
        ruleRepo = RuleRepository(RuleDatabase.getDatabase(context).ruleDao())
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

    override suspend fun getRule(libName: String, @LibType type: Int, useRegex: Boolean): Rule? {
        ruleRepo?.getRule(libName)?.let {
            return Rule(
                it.label,
                IconResMap.getIconRes(it.iconIndex),
                getDescriptionUrl(it),
                it.regexName,
                IconResMap.isSingleColorIcon(it.iconIndex)
            )
        }
        if (useRegex) {
            findRuleRegex(libName, type)?.let {
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
            LCRemoteRepo.Gitee -> Urls.GITEE_ROOT_URL
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

    private fun findRuleRegex(string: String, @LibType type: Int): RuleEntity? {
        RuleRepository.regexRules?.let {
            val iterator = it.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (entry.key.matcher(string).matches() && entry.value.type == type) {
                    return entry.value
                }
            }
        }
        return null
    }
}