package com.absinthe.rulesbundle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class RuleRepository(private val ruleDao: RuleDao) {

    init {
        GlobalScope.launch(Dispatchers.IO) {
            if (rules == null) {
                rules = getAllRules().asSequence()
                    .map {
                        it.name to it
                    }
                    .toMap()
            }
            if (regexRules == null) {
                regexRules = getRegexRules().asSequence()
                    .map { Pattern.compile(it.name) to it }
                    .toMap()
            }
        }
    }

    fun getRule(name: String) = rules?.get(name)

    suspend fun insertRules(rules: List<RuleEntity>) {
        ruleDao.insertRules(rules)
    }

    fun deleteAllRules() {
        ruleDao.deleteAllRules()
    }

    suspend fun getAllRules() = ruleDao.getAllRules()

    suspend fun getRegexRules() = ruleDao.getRegexRules()

    companion object {
        var rules: Map<String, RuleEntity>? = null
        var regexRules: Map<Pattern, RuleEntity>? = null
    }
}
