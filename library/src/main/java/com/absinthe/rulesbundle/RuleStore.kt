package com.absinthe.rulesbundle

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import java.io.Closeable
import java.io.File
import java.util.regex.Pattern

internal class RuleStore private constructor(
    private val database: SQLiteDatabase
) : Closeable {

    private var indexes: RuleIndexes? = null

    fun findRule(name: String, @LibType type: Int, useRegex: Boolean): RuleRecord? {
        return findExactRule(name, type) ?: if (useRegex) findRegexRule(name, type) else null
    }

    override fun close() {
        database.close()
    }

    private fun findExactRule(name: String, type: Int): RuleRecord? {
        return getIndexes().exact[type]?.get(name)
    }

    private fun findRegexRule(name: String, type: Int): RuleRecord? {
        return getIndexes().regex[type]
            ?.firstOrNull { it.pattern.matcher(name).matches() }
            ?.record
    }

    private fun getIndexes(): RuleIndexes {
        indexes?.let { return it }
        val exact = mutableMapOf<Int, MutableMap<String, RuleRecord>>()
        val regex = mutableMapOf<Int, MutableList<RegexRule>>()
        database.query(TABLE_RULES, null, null, null, null, null, null).use { cursor ->
            while (cursor.moveToNext()) {
                val record = cursor.toRuleRecord()
                exact.getOrPut(record.type) { mutableMapOf() }[record.name] = record
                if (record.isRegexRule) {
                    regex.getOrPut(record.type) { mutableListOf() }
                        .add(RegexRule(Pattern.compile(record.name), record))
                }
            }
        }
        return RuleIndexes(exact, regex).also { indexes = it }
    }

    private data class RuleIndexes(
        val exact: Map<Int, Map<String, RuleRecord>>,
        val regex: Map<Int, List<RegexRule>>
    )

    private fun Cursor.toRuleRecord(): RuleRecord {
        return RuleRecord(
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
    val name: String,
    val label: String,
    @param:LibType val type: Int,
    val iconIndex: Int,
    val isRegexRule: Boolean,
    val regexName: String?
)
