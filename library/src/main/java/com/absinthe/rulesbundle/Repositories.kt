package com.absinthe.rulesbundle

import android.content.Context
import com.absinthe.rulesbundle.utils.FileUtils
import com.absinthe.rulesbundle.utils.HashUtils
import java.io.File

object Repositories {

    const val RULES_DATABASE_NAME = "rules_database"
    private const val LOCAL_RULES_VERSION_FILE = "lcrules/version"

    fun checkRulesDatabase(context: Context) {
        if (!checkMd5(context) && !checkVersion(context)) {
            deleteRulesDatabase(context)
        }
    }

    fun getLocalRulesVersion(context: Context): Int {
        val localCloudVersionFile = File(context.filesDir, LOCAL_RULES_VERSION_FILE)
        if (localCloudVersionFile.exists().not()) return LCRules.getVersion()
        if (localCloudVersionFile.isDirectory.not()) return LCRules.getVersion()
        localCloudVersionFile.listFiles()?.takeIf { it.isNotEmpty() }?.let { files ->
            return runCatching { files.maxOf { it.name.toInt() } }.getOrDefault(LCRules.getVersion())
        } ?: return LCRules.getVersion()
    }

    fun setLocalRulesVersion(context: Context, version: Int): Boolean {
        val localCloudVersionFile = File(context.filesDir, LOCAL_RULES_VERSION_FILE)
        if (localCloudVersionFile.isDirectory.not()) localCloudVersionFile.delete()
        if (localCloudVersionFile.exists().not()) localCloudVersionFile.mkdirs()
        return File(localCloudVersionFile, version.toString()).createNewFile()
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
        val versionFile = File(context.filesDir, LOCAL_RULES_VERSION_FILE)
        if (versionFile.exists().not()) return false
        if (versionFile.isDirectory.not()) return false
        versionFile.listFiles()?.takeIf { it.isNotEmpty() }?.let { files ->
            val version = runCatching { files.maxOf { it.name.toInt() } }.getOrDefault(0)
            return version >= LCRules.getVersion()
        } ?: return false
    }
}