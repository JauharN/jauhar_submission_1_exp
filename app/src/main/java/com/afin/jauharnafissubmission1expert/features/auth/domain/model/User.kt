package com.afin.jauharnafissubmission1expert.features.auth.domain.model

data class User(
    val userId: String,
    val name: String,
    val token: String,
    val isLogin: Boolean = false
)