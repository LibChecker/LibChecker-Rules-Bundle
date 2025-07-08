package com.absinthe.rulesbundle

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RuleDao(context: Context) {
    private val dbHelper = RuleDatabaseHelper(context)

    fun closeDb() {
        dbHelper.close()
    }

    suspend fun insertRules(items: List<RuleEntity>) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for (item in items) {
                val values = ContentValues().apply {
                    put("_id", item.id)
                    put("name", item.name)
                    put("label", item.label)
                    put("type", item.type)
                    put("iconIndex", item.iconIndex)
                    put("isRegexRule", if (item.isRegexRule) 1 else 0)
                    put("regexName", item.regexName)
                }
                db.insertWithOnConflict("rules_table", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun deleteAllRules() {
        val db = dbHelper.writableDatabase
        db.delete("rules_table", null, null)
        db.close()
    }

    suspend fun getRule(name: String): RuleEntity? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(
            "rules_table",
            null,
            "name LIKE ?",
            arrayOf(name),
            null, null, null
        ).use { cursor ->
            val entity = if (cursor.moveToFirst()) cursorToEntity(cursor) else null
            db.close()
            entity
        }
    }

    suspend fun getAllRules(): List<RuleEntity> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query("rules_table", null, null, null, null, null, null).use { cursor ->
            val list = mutableListOf<RuleEntity>()
            while (cursor.moveToNext()) {
                list.add(cursorToEntity(cursor))
            }
            db.close()
            list
        }
    }

    suspend fun getRegexRules(): List<RuleEntity> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        db.query(
            "rules_table",
            null,
            "isRegexRule = 1",
            null, null, null, null
        ).use { cursor ->
            val list = mutableListOf<RuleEntity>()
            while (cursor.moveToNext()) {
                list.add(cursorToEntity(cursor))
            }
            db.close()
            list
        }
    }

    fun selectAllRules(): Cursor? {
        val db = dbHelper.readableDatabase
        return db.query("rules_table", null, null, null, null, null, null)
    }

    fun selectRuleByName(name: String): Cursor? {
        val db = dbHelper.readableDatabase
        return db.query(
            "rules_table",
            null,
            "name LIKE ?",
            arrayOf(name),
            null, null, null
        )
    }

    private fun cursorToEntity(cursor: Cursor): RuleEntity {
        return RuleEntity(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            label = cursor.getString(cursor.getColumnIndexOrThrow("label")),
            type = cursor.getInt(cursor.getColumnIndexOrThrow("type")),
            iconIndex = cursor.getInt(cursor.getColumnIndexOrThrow("iconIndex")),
            isRegexRule = cursor.getInt(cursor.getColumnIndexOrThrow("isRegexRule")) == 1,
            regexName = cursor.getString(cursor.getColumnIndexOrThrow("regexName"))
        )
    }
}
