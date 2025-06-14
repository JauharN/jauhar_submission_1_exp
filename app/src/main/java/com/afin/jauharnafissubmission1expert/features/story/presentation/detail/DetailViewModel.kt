package com.afin.jauharnafissubmission1expert.features.story.presentation.detail

import androidx.lifecycle.ViewModel
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository

class DetailViewModel(private val repository: StoryRepository) : ViewModel() {

    fun getStoryDetail(id: String) = repository.getStoryDetail(id)
}