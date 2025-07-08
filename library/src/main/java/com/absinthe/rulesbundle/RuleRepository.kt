package com.absinthe.rulesbundle

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class RuleRepository(context: Context) {
    private val ruleDao = RuleDao(context)

    private var rulesCache: Map<String, RuleEntity>? = null
    private var regexRulesCache: Map<Pattern, RuleEntity>? = null

    suspend fun getRule(name: String): RuleEntity? {
        if (rulesCache == null) {
            rulesCache = getAllRules().associateBy { it.name }
        }
        return rulesCache?.get(name)
    }

    suspend fun insertRules(rules: List<RuleEntity>) = withContext(Dispatchers.IO) {
        ruleDao.insertRules(rules)
        rulesCache = null
        regexRulesCache = null
    }

    suspend fun deleteAllRules() = withContext(Dispatchers.IO) {
        ruleDao.deleteAllRules()
        rulesCache = null
        regexRulesCache = null
    }

    suspend fun getAllRules(): List<RuleEntity> = ruleDao.getAllRules()

    suspend fun getRegexRules(): Map<Pattern, RuleEntity> {
        if (regexRulesCache == null) {
            regexRulesCache = ruleDao.getRegexRules().associateBy({ Pattern.compile(it.name) }, { it })
        }
        return regexRulesCache ?: emptyMap()
    }

    fun closeDb() {
        ruleDao.closeDb()
    }
}
