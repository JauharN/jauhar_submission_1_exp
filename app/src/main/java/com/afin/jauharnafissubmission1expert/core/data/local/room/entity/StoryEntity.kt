package com.afin.jauharnafissubmission1expert.core.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity (
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val createdAt: String,
    val lat: Double?,
    val lon: Double?,
)