package com.example.manapeliculas.data.Series

data class SeriesInfo(
    val TMDbId: String,
    val cast: Cast,
    val genres: List<Genre>,
    val images: Images,
    val overview: String,
    val rate: Rate,
    val releaseDate: String,
    val seasons: List<Season>,
    val slug: SlugX,
    val titles: Titles
)