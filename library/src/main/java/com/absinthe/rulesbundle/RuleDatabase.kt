package com.absinthe.rulesbundle

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RuleEntity::class], version = 1)
abstract class RuleDatabase : RoomDatabase() {

    abstract fun ruleDao(): RuleDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: RuleDatabase? = null

        fun getDatabase(context: Context): RuleDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    RuleDatabase::class.java,
                    "rule_database"
                ).fallbackToDestructiveMigration()
                    .createFromAsset("lcrules/rules.db")
                INSTANCE = builder.build()
                return INSTANCE!!
            }
        }
    }
}
