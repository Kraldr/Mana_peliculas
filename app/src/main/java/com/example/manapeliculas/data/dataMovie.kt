package com.example.manapeliculas.data

data class dataMovie(
    val duracion: String,
    val hrefIMG: String,
    val mutable: List<MutableX>,
    val originalTitle: String,
    val rate: String,
    val sinopsis: String,
    val tags: String,
    val titulo: String,
    val year: String
)