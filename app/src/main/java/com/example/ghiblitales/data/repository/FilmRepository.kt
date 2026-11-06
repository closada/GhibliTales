package com.example.ghiblitales.data.repository

import com.example.ghiblitales.data.FilmModel
import com.example.ghiblitales.data.ddbb.FilmDbHelper
import com.example.ghiblitales.data.remote.RetrofitClient
import java.io.IOException
import retrofit2.HttpException

class FilmRepository(private val dbHelper: FilmDbHelper) {

    suspend fun fetchFilms(): List<FilmModel> {
        val films = try {
            RetrofitClient.apiService.getFilms()
        } catch (e: HttpException) {
            throw Exception("Server error (code ${e.code()})")
        } catch (e: IOException) {
            throw Exception("No internet connection or the request timed out")
        }

        if (films.isEmpty()) {
            throw Exception("The API returned no films")
        }

        return films.map { item ->
            item.copy(isFavorite = dbHelper.isFavorite(item.id))
        }
    }

    fun getFavorites(): List<FilmModel> {
        return dbHelper.getAllFavorites()
    }

    fun isFavorite(id: String): Boolean {
        return dbHelper.isFavorite(id)
    }

    fun toggleFavorite(item: FilmModel): Boolean {
        return if (item.isFavorite) {
            if (!dbHelper.deleteFavorite(item.id)) {
                throw Exception("Could not remove the film from favorites")
            }
            false
        } else {
            if (!dbHelper.insertFavorites(item)) {
                throw Exception("Could not save the film to favorites")
            }
            true
        }
    }
}