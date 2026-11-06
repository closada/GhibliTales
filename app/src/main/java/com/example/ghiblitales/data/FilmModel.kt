package com.example.ghiblitales.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FilmModel(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("original_title") val originalTitle: String,
    @SerializedName("image") val image: String,
    @SerializedName("movie_banner") val movieBanner: String,
    @SerializedName("description") val description: String,
    @SerializedName("director") val director: String,
    @SerializedName("producer") val producer: String,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("running_time") val runningTime: String,
    @SerializedName("rt_score") val rtScore: String,
    var isFavorite: Boolean = false
) : Serializable