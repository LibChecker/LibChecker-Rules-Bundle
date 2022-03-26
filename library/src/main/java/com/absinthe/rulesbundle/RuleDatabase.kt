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
        private var instance: RuleDatabase? = null

        fun getDatabase(context: Context): RuleDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): RuleDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                RuleDatabase::class.java,
                "rules_database"
            ).fallbackToDestructiveMigration()
                .createFromAsset("lcrules/rules.db")
                .build()
        }
    }
}
