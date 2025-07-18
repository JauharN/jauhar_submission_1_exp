package com.afin.jauharnafissubmission1expert.core.di

import android.content.Context
import com.afin.jauharnafissubmission1expert.core.data.local.preference.UserPreference
import com.afin.jauharnafissubmission1expert.core.data.local.room.StoryDatabase
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository
import com.afin.jauharnafissubmission1expert.core.utils.dataStore

object Injection {

    fun provideRepository(context: Context): StoryRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val database = StoryDatabase.getInstance(context)
        return StoryRepository.getInstance(pref, database)
    }

    fun provideUserPreference(context: Context): UserPreference {
        return UserPreference.getInstance(context.dataStore)
    }

    fun provideDatabase(context: Context): StoryDatabase {
        return StoryDatabase.getInstance(context)
    }
}