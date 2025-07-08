package com.afin.jauharnafissubmission1expert.core.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.*
import com.afin.jauharnafissubmission1expert.core.data.local.preference.UserPreference
import com.afin.jauharnafissubmission1expert.core.data.local.room.StoryDatabase
import com.afin.jauharnafissubmission1expert.core.data.paging.StoryRemoteMediator
import com.afin.jauharnafissubmission1expert.core.data.remote.api.ApiConfig
import com.afin.jauharnafissubmission1expert.core.data.remote.response.ErrorResponse
import com.afin.jauharnafissubmission1expert.core.utils.DataMapper
import com.afin.jauharnafissubmission1expert.core.utils.Result
import com.afin.jauharnafissubmission1expert.features.auth.domain.model.User
import com.afin.jauharnafissubmission1expert.features.story.domain.model.Story
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

@OptIn(ExperimentalPagingApi::class)
class StoryRepository private constructor(
    private val userPreference: UserPreference,
    private val database: StoryDatabase
) {

    // Ambil instance dari ApiService dengan token user yang tersimpan
    private suspend fun getApiService(): com.afin.jauharnafissubmission1expert.core.data.remote.api.ApiService {
        val token = userPreference.getUser().first().token
        return ApiConfig.getApiService(token)
    }

    // Fungsi register user ke server
    fun register(name: String, email: String, password: String): LiveData<Result<String>> =
        liveData {
            emit(Result.Loading)
            try {
                val apiService = ApiConfig.getApiService()
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

    // Fungsi login user dan simpan data user di local preferences
    fun login(email: String, password: String): LiveData<Result<User>> = liveData {
        emit(Result.Loading)
        try {
            val apiService = ApiConfig.getApiService()
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

    // Fungsi baru: Mengambil daftar cerita menggunakan Paging 3 dan sinkronisasi ke Room
    fun getStoriesWithPaging(): Flow<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,              // Ukuran halaman per page
                enablePlaceholders = false // Tidak menampilkan placeholder saat loading
            ),
            remoteMediator = StoryRemoteMediator(
                database = database,
                apiService = ApiConfig.getApiService(getToken()) // Ambil token dari local prefs
            ),
            pagingSourceFactory = {
                database.storyDao().getAllStories() // Ambil data dari database lokal Room
            }
        ).flow.map { pagingData ->
            pagingData.map { entity ->
                DataMapper.mapStoryEntityToDomain(entity) // Ubah dari Entity ke Domain Model
            }
        }
    }

    // Fungsi fallback untuk mengambil semua story langsung dari API (non-paging)
    fun getStories(): LiveData<Result<List<Story>>> = liveData {
        emit(Result.Loading)
        try {
            val apiService = getApiService()
            val response = apiService.getStories()
            if (!response.error) {
                val stories = response.listStory.map { DataMapper.mapStoryResponseToDomain(it) }
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

    // Fungsi untuk menampilkan story yang memiliki koordinat (digunakan untuk Map)
    fun getStoriesWithLocation(): LiveData<Result<List<Story>>> = liveData {
        emit(Result.Loading)
        try {
            val apiService = getApiService()
            val response = apiService.getStories(location = 1)
            if (!response.error) {
                val stories = response.listStory.map { DataMapper.mapStoryResponseToDomain(it) }
                emit(Result.Success(stories))
            } else {
                emit(Result.Error(response.message))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(Result.Error(errorResponse.message ?: "Failed to load stories with location"))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown error occurred"))
        }
    }

    // Ambil detail cerita berdasarkan ID
    fun getStoryDetail(id: String): LiveData<Result<Story>> = liveData {
        emit(Result.Loading)
        try {
            val apiService = getApiService()
            val response = apiService.getStoryDetail(id)
            if (!response.error) {
                val story = DataMapper.mapStoryResponseToDomain(response.story)
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

    // Fungsi untuk upload cerita ke server dengan gambar dan deskripsi
    fun uploadStory(
        file: File,
        description: String,
        lat: Double? = null,
        lon: Double? = null
    ): LiveData<Result<String>> = liveData {
        emit(Result.Loading)
        try {
            val apiService = getApiService()

            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart =
                MultipartBody.Part.createFormData("photo", file.name, requestImageFile)

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

    // Ambil user yang sedang login dari preference
    fun getUser(): Flow<User> = userPreference.getUser()

    // Hapus data user saat logout
    suspend fun logout() {
        userPreference.logout()
    }

    // Fungsi utilitas untuk ambil token secara blocking-safe
    private fun getToken(): String {
        return runCatching {
            kotlinx.coroutines.runBlocking {
                userPreference.getUser().first().token
            }
        }.getOrDefault("")
    }

    // Singleton Pattern
    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(
            userPreference: UserPreference,
            database: StoryDatabase
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(userPreference, database)
            }.also { instance = it }
    }
}
