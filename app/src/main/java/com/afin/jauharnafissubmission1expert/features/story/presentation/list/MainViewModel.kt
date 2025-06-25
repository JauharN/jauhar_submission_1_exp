package com.afin.jauharnafissubmission1expert.features.story.presentation.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository
import com.afin.jauharnafissubmission1expert.core.utils.Result
import com.afin.jauharnafissubmission1expert.features.story.domain.model.Story
import kotlinx.coroutines.launch

class MainViewModel(private val repository: StoryRepository) : ViewModel() {

    // Digunakan saat onCreate
    val stories: LiveData<Result<List<Story>>> = repository.getStories()

    // Digunakan saat swipe refresh dan onResume
    fun refreshStories(): LiveData<Result<List<Story>>> = repository.getStories()

    fun getUser() = repository.getUser()

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
