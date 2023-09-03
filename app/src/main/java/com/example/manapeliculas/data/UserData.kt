package com.example.manapeliculas.data

data class UserData(
    val userId: String,
    val tags: String,
    val isLoggedIn: Boolean,
    val name: String,
    val userImage: String
)