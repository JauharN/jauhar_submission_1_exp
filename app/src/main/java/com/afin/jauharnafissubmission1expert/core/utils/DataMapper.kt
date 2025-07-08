package com.afin.jauharnafissubmission1expert.core.utils

import com.afin.jauharnafissubmission1expert.core.data.local.room.entity.StoryEntity
import com.afin.jauharnafissubmission1expert.core.data.remote.response.StoryItem
import com.afin.jauharnafissubmission1expert.features.story.domain.model.Story

object DataMapper {

    fun mapStoryEntityToDomain(entity: StoryEntity): Story {
        return Story(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            photoUrl = entity.photoUrl,
            createdAt = entity.createdAt,
            lat = entity.lat,
            lon = entity.lon
        )
    }

    fun mapStoryResponseToEntity(response: StoryItem): StoryEntity {
        return StoryEntity(
            id = response.id,
            name = response.name,
            description = response.description,
            photoUrl = response.photoUrl,
            createdAt = response.createdAt,
            lat = response.lat,
            lon = response.lon
        )
    }

    fun mapStoryResponseToDomain(response: StoryItem): Story {
        return Story(
            id = response.id,
            name = response.name,
            description = response.description,
            photoUrl = response.photoUrl,
            createdAt = response.createdAt,
            lat = response.lat,
            lon = response.lon
        )
    }
}