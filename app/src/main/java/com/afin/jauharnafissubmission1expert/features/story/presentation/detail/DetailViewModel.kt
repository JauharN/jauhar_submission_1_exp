package com.afin.jauharnafissubmission1expert.features.story.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository
import com.afin.jauharnafissubmission1expert.core.utils.Result
import com.afin.jauharnafissubmission1expert.features.story.domain.model.Story
import kotlinx.coroutines.launch

class DetailViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _storyDetail = MutableLiveData<Result<Story>>()
    val storyDetail: LiveData<Result<Story>> = _storyDetail

    fun getStoryDetail(id: String) {
        viewModelScope.launch {
            repository.getStoryDetail(id).observeForever { result ->
                _storyDetail.value = result
            }
        }
    }
}