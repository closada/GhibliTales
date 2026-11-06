package com.example.ghiblitales.data.remote

import com.example.ghiblitales.data.FilmModel
import retrofit2.http.GET

interface ApiService {

    @GET("films")
    suspend fun getFilms() : List<FilmModel>
}