package com.afin.jauharnafissubmission1expert.features.auth.presentation.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository
import com.afin.jauharnafissubmission1expert.core.utils.Event
import com.afin.jauharnafissubmission1expert.core.utils.Result
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<Event<Result<String>>>()
    val registerResult: LiveData<Event<Result<String>>> = _registerResult

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            repository.register(name, email, password).observeForever { result ->
                _registerResult.value = Event(result)
            }
        }
    }
}