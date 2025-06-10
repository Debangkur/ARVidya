package com.example.learnui

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.learnui.DataClass.LocalModels

@Database(
    entities = [LocalModels::class],
    version = 1,
)
abstract class LocalModelDatabase: RoomDatabase() {
    abstract val dao: LocalModelsDao

    companion object {
        @Volatile
        private var INSTANCE: LocalModelDatabase? = null

        fun getDatabase(context: Context): LocalModelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalModelDatabase::class.java,
                    "local_model_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
