package com.absinthe.rulesbundle

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.io.File
import java.util.regex.Pattern

internal class RuleStore private constructor(
    private val database: SQLiteDatabase
) : Closeable {

    private var ruleIndex: Map<Int, Map<String, RuleRecord>>? = null
    private var regexIndex: Map<Int, List<RegexRule>>? = null

    suspend fun findRule(name: String, @LibType type: Int, useRegex: Boolean): RuleRecord? {
        return withContext(Dispatchers.IO) {
            findExactRule(name, type) ?: if (useRegex) findRegexRule(name, type) else null
        }
    }

    override fun close() {
        database.close()
    }

    private fun findExactRule(name: String, type: Int): RuleRecord? {
        return getRuleIndex()[type]?.get(name)
    }

    private fun findRegexRule(name: String, type: Int): RuleRecord? {
        return getRegexIndex()[type]
            ?.firstOrNull { it.pattern.matcher(name).matches() }
            ?.record
    }

    private fun getRuleIndex(): Map<Int, Map<String, RuleRecord>> {
        ruleIndex?.let { return it }
        return loadRules()
            .groupBy { it.type }
            .mapValues { (_, rules) -> rules.associateBy { it.name } }
            .also { ruleIndex = it }
    }

    private fun getRegexIndex(): Map<Int, List<RegexRule>> {
        regexIndex?.let { return it }
        return loadRules()
            .filter { it.isRegexRule }
            .groupBy { it.type }
            .mapValues { (_, rules) ->
                rules.map { rule -> RegexRule(Pattern.compile(rule.name), rule) }
            }
            .also { regexIndex = it }
    }

    private fun loadRules(): List<RuleRecord> {
        return database.query(TABLE_RULES, null, null, null, null, null, null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toRuleRecord())
                }
            }
        }
    }

    private fun Cursor.toRuleRecord(): RuleRecord {
        return RuleRecord(
            id = getInt(getColumnIndexOrThrow(COLUMN_ID)),
            name = getString(getColumnIndexOrThrow(COLUMN_NAME)),
            label = getString(getColumnIndexOrThrow(COLUMN_LABEL)),
            type = getInt(getColumnIndexOrThrow(COLUMN_TYPE)),
            iconIndex = getInt(getColumnIndexOrThrow(COLUMN_ICON_INDEX)),
            isRegexRule = getInt(getColumnIndexOrThrow(COLUMN_IS_REGEX_RULE)) == 1,
            regexName = getString(getColumnIndexOrThrow(COLUMN_REGEX_NAME))
        )
    }

    private data class RegexRule(
        val pattern: Pattern,
        val record: RuleRecord
    )

    companion object {
        private const val TABLE_RULES = "rules_table"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_LABEL = "label"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_ICON_INDEX = "iconIndex"
        private const val COLUMN_IS_REGEX_RULE = "isRegexRule"
        private const val COLUMN_REGEX_NAME = "regexName"

        fun open(context: Context, assetPath: String, version: Int): RuleStore {
            val databaseFile = prepareDatabaseFile(context, assetPath, version)
            return RuleStore(openDatabase(databaseFile) {
                databaseFile.delete()
                copyAsset(context, assetPath, databaseFile)
            })
        }

        private fun prepareDatabaseFile(context: Context, assetPath: String, version: Int): File {
            val directory = File(context.noBackupFilesDir, "lcrules").apply { mkdirs() }
            val databaseFile = File(directory, "rules-v$version.db")
            if (!databaseFile.exists() || databaseFile.length() == 0L) {
                copyAsset(context, assetPath, databaseFile)
            }
            directory.listFiles()
                ?.filter { it.name.startsWith("rules-v") && it.name != databaseFile.name }
                ?.forEach { it.delete() }
            return databaseFile
        }

        private fun openDatabase(databaseFile: File, retry: () -> Unit): SQLiteDatabase {
            return try {
                SQLiteDatabase.openDatabase(
                    databaseFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )
            } catch (exception: SQLiteException) {
                retry()
                SQLiteDatabase.openDatabase(
                    databaseFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY
                )
            }
        }

        private fun copyAsset(context: Context, assetPath: String, databaseFile: File) {
            databaseFile.parentFile?.mkdirs()
            context.assets.open(assetPath).use { input ->
                databaseFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}

internal data class RuleRecord(
    val id: Int,
    val name: String,
    val label: String,
    @LibType val type: Int,
    val iconIndex: Int,
    val isRegexRule: Boolean,
    val regexName: String?
)
