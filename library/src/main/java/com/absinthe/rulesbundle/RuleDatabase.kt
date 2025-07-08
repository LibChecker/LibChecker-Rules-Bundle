package com.absinthe.rulesbundle

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RuleDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    Repositories.RULES_DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        // Prebuilt database, no need to create tables
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Prebuilt database, no upgrade logic needed
    }

    companion object {
        private const val DATABASE_VERSION = 1
    }
}
