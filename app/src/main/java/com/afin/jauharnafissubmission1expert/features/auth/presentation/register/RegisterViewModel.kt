package com.afin.jauharnafissubmission1expert.features.auth.presentation.register

import androidx.lifecycle.ViewModel
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository

class RegisterViewModel(private val repository: StoryRepository) : ViewModel() {

    fun register(name: String, email: String, password: String) =
        repository.register(name, email, password)
}