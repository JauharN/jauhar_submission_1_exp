package com.afin.jauharnafissubmission1expert.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.afin.jauharnafissubmission1expert.core.data.local.room.StoryDatabase
import com.afin.jauharnafissubmission1expert.core.data.local.room.entity.RemoteKeys
import com.afin.jauharnafissubmission1expert.core.data.local.room.entity.StoryEntity
import com.afin.jauharnafissubmission1expert.core.data.remote.api.ApiService
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val database: StoryDatabase,
    private val apiService: ApiService
) : RemoteMediator<Int, StoryEntity>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                prevKey
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey ?: return MediatorResult.Success(
                    endOfPaginationReached = remoteKeys != null
                )
                nextKey
            }
        }

        try {
            val responseData = apiService.getStories(page, state.config.pageSize)
            val stories = responseData.listStory
            val endOfPaginationReached = stories.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.storyDao().deleteAllStories()
                    database.storyDao().deleteRemoteKeys()
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                val keys = stories.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                database.storyDao().insertRemoteKeys(keys)

                val storyEntities = stories.map { story ->
                    StoryEntity(
                        id = story.id,
                        name = story.name,
                        description = story.description,
                        photoUrl = story.photoUrl,
                        createdAt = story.createdAt,
                        lat = story.lat,
                        lon = story.lon
                    )
                }
                database.storyDao().insertStories(storyEntities)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.storyDao().getRemoteKeysId(id)
            }
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.storyDao().getRemoteKeysId(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.storyDao().getRemoteKeysId(data.id)
        }
    }

    companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

}