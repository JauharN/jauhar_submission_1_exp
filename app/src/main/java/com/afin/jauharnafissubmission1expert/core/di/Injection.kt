package com.afin.jauharnafissubmission1expert.core.di

import android.content.Context
import com.afin.jauharnafissubmission1expert.core.data.local.preference.UserPreference
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository
import com.afin.jauharnafissubmission1expert.core.utils.dataStore

object Injection {

    fun provideRepository(context: Context): StoryRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        return StoryRepository.getInstance(pref)
    }

    fun provideUserPreference(context: Context): UserPreference {
        return UserPreference.getInstance(context.dataStore)
    }
}