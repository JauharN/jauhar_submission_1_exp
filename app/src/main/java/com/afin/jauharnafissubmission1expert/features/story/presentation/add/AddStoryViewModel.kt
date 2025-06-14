package com.afin.jauharnafissubmission1expert.features.story.presentation.add

import androidx.lifecycle.ViewModel
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository
import java.io.File

class AddStoryViewModel(private val repository: StoryRepository) : ViewModel() {

    fun uploadStory(file: File, description: String) =
        repository.uploadStory(file, description)

    fun uploadStoryWithLocation(
        file: File,
        description: String,
        lat: Double,
        lon: Double
    ) = repository.uploadStory(file, description, lat, lon)
}