package com.pobvol.pobvolchecklists.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities =
    [RecordEntity::class, ChecklistEntity::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
    abstract fun checklistDao(): ChecklistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database.sqlite3"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
