package com.afin.jauharnafissubmission1expert.core.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.afin.jauharnafissubmission1expert.core.data.local.room.dao.StoryDao
import com.afin.jauharnafissubmission1expert.core.data.local.room.entity.RemoteKeys
import com.afin.jauharnafissubmission1expert.core.data.local.room.entity.StoryEntity

@Database(
    entities = [StoryEntity::class, RemoteKeys::class],
    version = 1,
    exportSchema = false
)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao

    companion object {
        @Volatile
        private var INSTANCE: StoryDatabase? = null

        fun getInstance(context: Context): StoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): StoryDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                StoryDatabase::class.java,
                "story_database"
            ).fallbackToDestructiveMigration().build()
        }
    }
}