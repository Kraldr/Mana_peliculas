package com.example.manapeliculas.data

import com.example.manapeliculas.data.cuevana2.LastP

data class User(
    val email: String,
    val like: MutableList<LastP>,
    val myList: MutableList<LastP>,
    val name: String,
    val tags: String,
    val userID: String,
    val userImage: String
) {
    // Agrega un constructor sin argumentos (necesario para Firebase)
    constructor() : this("", mutableListOf(), mutableListOf(), "", "", "", "")
}