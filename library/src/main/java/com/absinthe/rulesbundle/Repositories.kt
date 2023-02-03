package com.absinthe.rulesbundle

import android.content.Context
import com.absinthe.rulesbundle.utils.FileUtils
import com.absinthe.rulesbundle.utils.HashUtils
import java.io.File

object Repositories {

    const val RULES_DATABASE_NAME = "rules_database"

    fun checkRulesDatabase(context: Context) {
        if (!checkMd5(context) && !checkVersion(context)) {
            deleteRulesDatabase(context)
        }
    }

    private fun deleteRulesDatabase(context: Context) {
        val databaseDir = context.getDatabasePath(RULES_DATABASE_NAME).parent
        FileUtils.delete(File(databaseDir, RULES_DATABASE_NAME))
        FileUtils.delete(File(databaseDir, "${RULES_DATABASE_NAME}-shm"))
        FileUtils.delete(File(databaseDir, "${RULES_DATABASE_NAME}-wal"))
    }

    private fun checkMd5(context: Context): Boolean {
        val databaseDir = context.getDatabasePath(RULES_DATABASE_NAME).parent
        val localDB = File(databaseDir, RULES_DATABASE_NAME)
        if (localDB.exists().not()) return false
        val localDBMd5 = HashUtils.md5(localDB.readBytes())
        val assetDBMd5 = context.assets.open(LCRules.getRulesAssetPath()).use {
            HashUtils.md5(it.readBytes())
        }
        return localDBMd5 == assetDBMd5
    }

    private fun checkVersion(context: Context): Boolean {
        val versionFile = File(context.filesDir, "lcrules/version")
        if (versionFile.exists().not()) return false
        if (versionFile.isDirectory.not()) return false
        versionFile.listFiles()?.takeIf { it.isNotEmpty() }?.let {
            val version = runCatching { it[0].name.toInt() }.getOrDefault(0)
            return version >= LCRules.getVersion()
        } ?: return false
    }
}