package com.afin.jauharnafissubmission1expert.utils

import com.afin.jauharnafissubmission1expert.features.story.domain.model.Story

object DataDummy {

    fun generateDummyStoryResponse(): List<Story> {
        val items: MutableList<Story> = arrayListOf()
        for (i in 0..100) {
            val story = Story(
                id = "story-$i",
                name = "User $i",
                description = "Description $i",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-$i.png",
                createdAt = "2025-01-01T00:00:00Z",
                lat = i.toDouble(),
                lon = i.toDouble()
            )
            items.add(story)
        }
        return items
    }
}