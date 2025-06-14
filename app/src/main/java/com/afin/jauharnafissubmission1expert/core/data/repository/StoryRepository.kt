package com.afin.jauharnafissubmission1expert.core.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.afin.jauharnafissubmission1expert.core.data.local.preference.UserPreference
import com.afin.jauharnafissubmission1expert.core.data.remote.api.ApiService
import com.afin.jauharnafissubmission1expert.core.data.remote.response.ErrorResponse
import com.afin.jauharnafissubmission1expert.core.utils.Result
import com.afin.jauharnafissubmission1expert.features.auth.domain.model.User
import com.afin.jauharnafissubmission1expert.features.story.domain.model.Story
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {

    // Authentication Functions
    fun register(name: String, email: String, password: String): LiveData<Result<String>> =
        liveData {
            emit(Result.Loading)
            try {
                val response = apiService.register(name, email, password)
                if (!response.error) {
                    emit(Result.Success(response.message))
                } else {
                    emit(Result.Error(response.message))
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                emit(Result.Error(errorResponse.message ?: "Registration failed"))
            } catch (e: Exception) {
                emit(Result.Error(e.message ?: "Unknown error occurred"))
            }
        }

    fun login(email: String, password: String): LiveData<Result<User>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.login(email, password)
            if (!response.error && response.loginResult != null) {
                val user = User(
                    userId = response.loginResult.userId,
                    name = response.loginResult.name,
                    token = response.loginResult.token,
                    isLogin = true
                )
                userPreference.saveUser(user)
                emit(Result.Success(user))
            } else {
                emit(Result.Error(response.message))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(Result.Error(errorResponse.message ?: "Login failed"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Story Functions
    fun getStories(): LiveData<Result<List<Story>>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getStories()
            if (!response.error) {
                val stories = response.listStory.map { item ->
                    Story(
                        id = item.id,
                        name = item.name,
                        description = item.description,
                        photoUrl = item.photoUrl,
                        createdAt = item.createdAt,
                        lat = item.lat,
                        lon = item.lon
                    )
                }
                emit(Result.Success(stories))
            } else {
                emit(Result.Error(response.message))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(Result.Error(errorResponse.message ?: "Failed to load stories"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }

    fun getStoryDetail(id: String): LiveData<Result<Story>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getStoryDetail(id)
            if (!response.error) {
                val story = Story(
                    id = response.story.id,
                    name = response.story.name,
                    description = response.story.description,
                    photoUrl = response.story.photoUrl,
                    createdAt = response.story.createdAt,
                    lat = response.story.lat,
                    lon = response.story.lon
                )
                emit(Result.Success(story))
            } else {
                emit(Result.Error(response.message))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(Result.Error(errorResponse.message ?: "Failed to load story detail"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }

    fun uploadStory(
        file: File,
        description: String,
        lat: Double? = null,
        lon: Double? = null
    ): LiveData<Result<String>> = liveData {
        emit(Result.Loading)
        try {
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            val requestDescription = description.toRequestBody("text/plain".toMediaType())
            val requestLat = lat?.toString()?.toRequestBody("text/plain".toMediaType())
            val requestLon = lon?.toString()?.toRequestBody("text/plain".toMediaType())

            val response = apiService.addStory(
                imageMultipart,
                requestDescription,
                requestLat,
                requestLon
            )

            if (!response.error) {
                emit(Result.Success(response.message))
            } else {
                emit(Result.Error(response.message))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(Result.Error(errorResponse.message ?: "Failed to upload story"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // User Session Functions
    fun getUser(): Flow<User> = userPreference.getUser()

    suspend fun logout() {
        userPreference.logout()
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, userPreference)
            }.also { instance = it }
    }
}