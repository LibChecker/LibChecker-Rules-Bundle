package com.absinthe.rulesbundle

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.absinthe.rulesbundle.utils.FileUtils
import com.absinthe.rulesbundle.utils.HashUtils
import kotlinx.coroutines.runBlocking
import java.io.File

object Repositories {

    const val RULES_DATABASE_NAME = "lcrules_database"
    private const val LOCAL_RULES_VERSION_FILE = "lcrules/version"

    fun checkRulesDatabase(context: Context) {
        val dbFile = context.getDatabasePath(RULES_DATABASE_NAME)

        if (dbFile.exists()) {
            if (isDatabaseValid(context)) {
                return
            } else {
                deleteRulesDatabase(context)
            }
        }

        createRulesDatabase(context, dbFile)
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
        if (versionFile.exists().not()) return true
        if (versionFile.isDirectory.not()) return true
        versionFile.listFiles()?.takeIf { it.isNotEmpty() }?.let { files ->
            val version = runCatching { files.maxOf { it.name.toInt() } }.getOrDefault(0)
            return version >= LCRules.getVersion()
        } ?: return true
    }

    private fun createRulesDatabase(context: Context, dbFile: File) {
        var copySuccess = false
        try {
            context.assets.open(LCRules.getRulesAssetPath()).use { input ->
                dbFile.parentFile?.mkdirs()
                dbFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (isDatabaseValid(context)) {
                copySuccess = true
                Log.i("LCRules", "Successfully copied prebuilt database")
            } else {
                dbFile.delete()
                Log.w("LCRules", "Copied database is invalid, will create new one")
            }
        } catch (e: Exception) {
            Log.w("LCRules", "Failed to copy prebuilt database: ${e.message}")
            if (dbFile.exists()) {
                dbFile.delete()
            }
        }

        if (!copySuccess) {
            val helper = RuleDatabaseHelper(context)
            helper.writableDatabase // Trigger table creation
            populateDefaultData(context, helper)
            helper.close()
            Log.i("LCRules", "Created database using SQLiteOpenHelper")
        }
    }

    private fun isDatabaseValid(context: Context): Boolean {
        return checkMd5(context) && checkVersion(context)
    }

    private fun populateDefaultData(context: Context, helper: RuleDatabaseHelper) {
        try {
            context.assets.open(LCRules.getRulesAssetPath()).use { assetStream ->
                val tempFile = File.createTempFile("temp_rules", ".db", context.cacheDir)
                tempFile.outputStream().use { output ->
                    assetStream.copyTo(output)
                }

                val tempHelper = object : SQLiteOpenHelper(
                    context, tempFile.absolutePath, null, 1
                ) {
                    override fun onCreate(db: SQLiteDatabase) {}
                    override fun onUpgrade(
                        db: SQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) {
                    }
                }

                val tempDb = tempHelper.readableDatabase
                val cursor = tempDb.query("rules_table", null, null, null, null, null, null)

                val ruleDao = RuleDao(context)
                val rules = mutableListOf<RuleEntity>()

                cursor.use {
                    while (it.moveToNext()) {
                        val rule = RuleEntity(
                            id = it.getInt(it.getColumnIndexOrThrow("_id")),
                            name = it.getString(it.getColumnIndexOrThrow("name")),
                            label = it.getString(it.getColumnIndexOrThrow("label")),
                            type = it.getInt(it.getColumnIndexOrThrow("type")),
                            iconIndex = it.getInt(it.getColumnIndexOrThrow("iconIndex")),
                            isRegexRule = it.getInt(it.getColumnIndexOrThrow("isRegexRule")) == 1,
                            regexName = it.getString(it.getColumnIndexOrThrow("regexName"))
                        )
                        rules.add(rule)
                    }
                }

                runBlocking {
                    ruleDao.insertRules(rules)
                }

                tempHelper.close()
                tempFile.delete()
                ruleDao.closeDb()

                Log.i(
                    "LCRules",
                    "Successfully populated database with ${rules.size} rules from asset"
                )
            }
        } catch (e: Exception) {
            Log.w(
                "LCRules",
                "Failed to populate data from asset database: ${e.message}"
            )
            Log.i("LCRules", "Database created with empty table structure")
        }
    }
}