package com.example.ghiblitales.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ghiblitales.data.FilmModel
import com.example.ghiblitales.data.ddbb.FilmDbHelper
import com.example.ghiblitales.data.repository.FilmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FilmRepository(FilmDbHelper(application))

    private var film: FilmModel? = null

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Called once from the Activity. It is ignored if the ViewModel already has the film (e.g. after a rotation).
    fun loadFilm(film: FilmModel) {
        if (this.film != null) return
        this.film = film
        _isFavorite.value = film.isFavorite
    }

    fun toggleFavorite() {
        val current = film ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // The repository decides whether to insert or delete based on the isFavorite flag
                _isFavorite.value = repository.toggleFavorite(current.copy(isFavorite = _isFavorite.value))
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}