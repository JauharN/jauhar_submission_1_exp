package com.afin.jauharnafissubmission1expert.features.auth.presentation.login

import androidx.lifecycle.ViewModel
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository

class LoginViewModel(private val repository: StoryRepository) : ViewModel() {

    fun login(email: String, password: String) = repository.login(email, password)

    fun getUser() = repository.getUser()
}