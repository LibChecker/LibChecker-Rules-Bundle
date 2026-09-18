package com.absinthe.rulesbundle

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import java.io.Closeable
import java.io.File
import java.util.regex.Pattern

/** Read-only reader. Downloading, installing and retaining bundles belong to the host app. */
class RuleReader private constructor(
    private val database: SQLiteDatabase,
    val metadata: RulesMetadata,
    private val exact: Map<Int, Map<String, RuleRecord>>,
    private val regex: Map<Int, List<Pair<Pattern, RuleRecord>>>
) : Closeable {
    private var closed = false

    @Synchronized
    @JvmOverloads
    fun getRule(libName: String, @LibType type: Int, useRegex: Boolean,
                remoteRepo: LCRemoteRepo = LCRemoteRepo.GitHub): Rule? {
        check(!closed) { "RuleReader is closed" }
        val record = exact[type]?.get(libName) ?: if (useRegex &&
            libName.none { it in "\n\r\u0085\u2028\u2029" }) {
            regex[type]?.firstOrNull { it.first.matcher(libName).matches() }?.second
        } else null
        return record?.let {
            Rule(libName, it.type, it.label, IconResMap.getIconRes(it.iconIndex),
                cloudDescriptionUrl(it, remoteRepo),
                it.regexName, IconResMap.isSingleColorIcon(it.iconIndex))
        }
    }

    @Synchronized
    override fun close() {
        if (!closed) {
            closed = true
            database.close()
        }
    }

    companion object {
        const val READER_VERSION = 5
        private val cloudDirectories = mapOf(NATIVE to "native-libs", SERVICE to "services-libs",
            ACTIVITY to "activities-libs", RECEIVER to "receivers-libs", PROVIDER to "providers-libs",
            DEX to "dex-libs", STATIC to "static-libs", ACTION to "actions-libs")
        private val requiredColumns = setOf("_id", "name", "label", "type", "iconIndex", "isRegexRule",
            "regexName", "priority")

        /**
         * Opens an explicit existing DB, checks integrity/schema and compiles every regex.
         * v5 requires only sibling metadata.json. Never repairs or deletes input.
         * Archive limits and digest verification belong to the app.
         */
        @JvmStatic
        fun open(databaseFile: File): RuleReader {
            require(databaseFile.isFile && databaseFile.length() > 0) { "Missing rules database" }
            val root = requireNotNull(databaseFile.canonicalFile.parentFile)
            val db = SQLiteDatabase.openDatabase(databaseFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            try {
                db.rawQuery("PRAGMA quick_check", null).use {
                    require(it.moveToFirst() && it.getString(0) == "ok" && !it.moveToNext()) {
                        "Invalid rules database integrity"
                    }
                }
                val version = db.version
                require(version == READER_VERSION) { "Unsupported rules schema: $version" }
                db.rawQuery("SELECT type FROM sqlite_master WHERE name='rules_table'", null).use {
                    require(it.moveToFirst() && it.getString(0) == "table") { "Missing rules table" }
                }
                val columns = mutableSetOf<String>()
                db.rawQuery("PRAGMA table_info(rules_table)", null).use {
                    while (it.moveToNext()) columns.add(it.getString(it.getColumnIndexOrThrow("name")))
                }
                require(columns == requiredColumns) { "Expected compact v5 rule columns" }
                val exact = mutableMapOf<Int, MutableMap<String, RuleRecord>>()
                val regex = mutableMapOf<Int, MutableList<Pair<Pattern, RuleRecord>>>()
                var count = 0
                db.query("rules_table", null, null, null, null, null,
                    "priority, _id").use { cursor ->
                    while (cursor.moveToNext()) {
                        val record = cursor.toRecord()
                        val previous = exact.getOrPut(record.type) { mutableMapOf() }.put(record.name, record)
                        require(previous == null) { "Duplicate rule type/name" }
                        if (record.isRegexRule) {
                            val pattern = portablePattern(record.name)
                            regex.getOrPut(record.type) { mutableListOf() }.add(Pattern.compile(pattern) to record)
                        }
                        count++
                    }
                }
                val metadataFile = File(root, "metadata.json").canonicalFile
                require(metadataFile.parentFile == root) { "Metadata must stay inside the bundle" }
                val metadata = RulesMetadata.read(metadataFile)
                require(metadata.ruleCount == count) { "Rules metadata count mismatch" }
                return RuleReader(db, metadata, exact, regex)
            } catch (failure: Throwable) {
                db.close()
                throw failure
            }
        }

        private fun Cursor.toRecord(): RuleRecord {
            val id = requiredLong("_id")
            val type = requiredLong("type")
            val regexFlag = requiredLong("isRegexRule")
            val iconIndex = requiredLong("iconIndex")
            require(id > 0 && type in 0..9 && regexFlag in 0..1 && iconIndex in -1..Int.MAX_VALUE) {
                "Invalid rule values"
            }
            require(requiredLong("priority") >= 0) { "Invalid regex priority" }
            return RuleRecord(requiredString("name"), requiredString("label"), type.toInt(), iconIndex.toInt(),
                regexFlag == 1L, nullableString("regexName"))
        }

        // Android uses ICU: unlike desktop Java, its default \\d matches Unicode digits.
        private fun portablePattern(pattern: String): String = buildString {
            var inClass = false
            var index = 0
            while (index < pattern.length) {
                val c = pattern[index++]
                if (c == '\\' && index < pattern.length) {
                    val escaped = pattern[index++]
                    if (escaped == 'd') append(if (inClass) "0-9" else "[0-9]")
                    else { append(c); append(escaped) }
                } else {
                    if (c == '[') inClass = true
                    if (c == ']') inClass = false
                    append(c)
                }
            }
        }

        private fun Cursor.requiredLong(name: String): Long {
            val column = getColumnIndexOrThrow(name)
            require(getType(column) == Cursor.FIELD_TYPE_INTEGER) { "Invalid integer: $name" }
            return getLong(column)
        }

        private fun Cursor.requiredString(name: String): String =
            requireNotNull(nullableString(name)) { "Missing string: $name" }

        private fun Cursor.nullableString(name: String): String? {
            val column = getColumnIndexOrThrow(name)
            if (isNull(column)) return null
            require(getType(column) == Cursor.FIELD_TYPE_STRING) { "Invalid string: $name" }
            return getString(column)
        }

        private fun cloudDescriptionUrl(record: RuleRecord, repo: LCRemoteRepo): String? {
            val directory = cloudDirectories[record.type] ?: return null
            return "${repo.rootUrl}$directory/${record.regexName ?: record.name}.json"
        }
    }
}

private class RuleRecord(
    val name: String,
    val label: String,
    val type: Int,
    val iconIndex: Int,
    val isRegexRule: Boolean,
    val regexName: String?
)
