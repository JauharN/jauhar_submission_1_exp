package com.afin.jauharnafissubmission1expert.features.story.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: StoryRepository) : ViewModel() {

    fun getStories() = repository.getStories()

    fun getUser() = repository.getUser()

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}