package com.afin.jauharnafissubmission1expert.features.story.presentation.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository
import com.afin.jauharnafissubmission1expert.core.utils.Event
import com.afin.jauharnafissubmission1expert.core.utils.Result
import com.afin.jauharnafissubmission1expert.features.story.domain.model.Story
import kotlinx.coroutines.launch

class MainViewModel(private val repository: StoryRepository) : ViewModel() {

    // State holders
    private val _stories = MutableLiveData<Result<List<Story>>>()
    val stories: LiveData<Result<List<Story>>> = _stories

    private val _refreshStories = MutableLiveData<Result<List<Story>>>()
    val refreshStories: LiveData<Result<List<Story>>> = _refreshStories

    private val _logoutEvent = MutableLiveData<Event<Boolean>>()
    val logoutEvent: LiveData<Event<Boolean>> = _logoutEvent

    init {
        getStories()
    }

    fun getStories() {
        viewModelScope.launch {
            repository.getStories().observeForever { result ->
                _stories.value = result
            }
        }
    }

    fun refreshStories() {
        viewModelScope.launch {
            repository.getStories().observeForever { result ->
                _refreshStories.value = result
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _logoutEvent.value = Event(true)
        }
    }
}