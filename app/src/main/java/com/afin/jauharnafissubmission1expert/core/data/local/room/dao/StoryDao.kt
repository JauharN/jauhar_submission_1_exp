package com.afin.jauharnafissubmission1expert.core.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.afin.jauharnafissubmission1expert.core.data.local.room.entity.RemoteKeys
import com.afin.jauharnafissubmission1expert.core.data.local.room.entity.StoryEntity

interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryEntity>)

    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    fun getAllStories(): PagingSource<Int, StoryEntity>

    @Query("DELETE FROM stories")
    suspend fun deleteAllStories()

    @Query("SELECT * FROM stories WHERE id = :storyId")
    suspend fun getStoryById(storyId: String): StoryEntity?

    // Remote Keys operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteKeys(remoteKey: List<RemoteKeys>)

    @Query("SELECT * FROM remote_keys WHERE id = :id")
    suspend fun getRemoteKeysId(id: String): RemoteKeys?

    @Query("DELETE FROM remote_keys")
    suspend fun deleteRemoteKeys()
}