package com.example.manapeliculas.data.Series

data class Episode(
    val TMDbId: String,
    val image: String,
    val number: Int,
    val releaseDate: String,
    val slug: Slug,
    val title: String
)