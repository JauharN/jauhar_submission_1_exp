package com.afin.jauharnafissubmission1expert.core.di

import android.content.Context
import com.afin.jauharnafissubmission1expert.core.data.local.preference.UserPreference
import com.afin.jauharnafissubmission1expert.core.data.remote.api.ApiConfig
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository
import com.afin.jauharnafissubmission1expert.core.utils.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {

    fun provideRepository(context: Context): StoryRepository {
        val pref = UserPreference.getInstance(context.dataStore)

        // Get token from DataStore
        val user = runBlocking { pref.getUser().first() }
        val apiService = ApiConfig.getApiService(user.token)

        return StoryRepository.getInstance(apiService, pref)
    }

    fun provideUserPreference(context: Context): UserPreference {
        return UserPreference.getInstance(context.dataStore)
    }
}