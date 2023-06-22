package com.example.manapeliculas.data.cuevana2

data class homeCuevana2(
    val carousel: List<Carousel>,
    val lastEpisodes: List<LastEpisode>,
    val lastP: List<LastP>,
    val lastS: List<LastS>,
    val pDestacadas: List<PDestacada>,
    val sDestacadas: List<SDestacada>
)