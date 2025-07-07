package com.afin.jauharnafissubmission1expert.features.auth.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository
import com.afin.jauharnafissubmission1expert.core.utils.Event
import com.afin.jauharnafissubmission1expert.core.utils.Result
import com.afin.jauharnafissubmission1expert.features.auth.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<Event<Result<User>>>()
    val loginResult: LiveData<Event<Result<User>>> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repository.login(email, password).observeForever { result ->
                _loginResult.value = Event(result)
            }
        }
    }

    fun getUser(): Flow<User> = repository.getUser()
}