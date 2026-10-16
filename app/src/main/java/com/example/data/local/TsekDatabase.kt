package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CheckEntity::class, LocalReportEntity::class, UserAccountEntity::class],
    version = 2,
    exportSchema = false
)
abstract class TsekDatabase : RoomDatabase() {
    abstract fun checkDao(): CheckDao

    companion object {
        @Volatile
        private var INSTANCE: TsekDatabase? = null

        fun getDatabase(context: Context): TsekDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TsekDatabase::class.java,
                    "tsek_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
