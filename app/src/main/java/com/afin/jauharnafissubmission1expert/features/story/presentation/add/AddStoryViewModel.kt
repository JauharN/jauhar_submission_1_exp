package com.afin.jauharnafissubmission1expert.features.story.presentation.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository
import com.afin.jauharnafissubmission1expert.core.utils.Event
import com.afin.jauharnafissubmission1expert.core.utils.Result
import kotlinx.coroutines.launch
import java.io.File

class AddStoryViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _uploadResult = MutableLiveData<Event<Result<String>>>()
    val uploadResult: LiveData<Event<Result<String>>> = _uploadResult

    fun uploadStory(file: File, description: String) {
        viewModelScope.launch {
            repository.uploadStory(file, description).observeForever { result ->
                _uploadResult.value = Event(result)
            }
        }
    }

    fun uploadStoryWithLocation(
        file: File,
        description: String,
        lat: Double,
        lon: Double
    ) {
        viewModelScope.launch {
            repository.uploadStory(file, description, lat, lon).observeForever { result ->
                _uploadResult.value = Event(result)
            }
        }
    }
}