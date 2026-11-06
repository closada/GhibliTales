package com.example.ghiblitales.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ghiblitales.data.FilmModel
import com.example.ghiblitales.data.ddbb.FilmDbHelper
import com.example.ghiblitales.data.repository.FilmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FilmRepository(FilmDbHelper(application))

    // Lista completa descargada de la API (o de favoritos si no hay conexión)
    private var allFilms: List<FilmModel> = emptyList()

    // Texto actual del buscador
    var currentQuery: String = ""
        private set

    // Lista que ve la pantalla: allFilms ya filtrada por currentQuery
    private val _filmList = MutableLiveData<List<FilmModel>>()
    val filmList: LiveData<List<FilmModel>> = _filmList

    private val _favoritesList = MutableLiveData<List<FilmModel>>()
    val favoritesList: LiveData<List<FilmModel>> = _favoritesList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadFilms() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            _errorMessage.postValue(null)
            try {
                allFilms = repository.fetchFilms()
            } catch (e: Exception) {
                _errorMessage.postValue(e.message ?: "Unknown error")
                // Sin conexión: mostramos lo guardado en SQLite
                allFilms = repository.getFavorites()
            } finally {
                applyFilter()
                _isLoading.postValue(false)
            }
        }
    }

    // El buscador trabaja sobre la lista en memoria: no hace falta otra llamada a la API
    fun searchFilms(query: String) {
        currentQuery = query
        applyFilter()
    }

    private fun applyFilter() {
        val query = currentQuery.trim()
        val filtered = if (query.isEmpty()) {
            allFilms
        } else {
            allFilms.filter { it.title.contains(query, ignoreCase = true) }
        }
        _filmList.postValue(filtered)
    }

    fun loadFavoritesFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getFavorites()
            _favoritesList.postValue(list)
        }
    }

    fun syncFavorites() {
        if (allFilms.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            allFilms = allFilms.map { it.copy(isFavorite = repository.isFavorite(it.id)) }
            applyFilter()
        }
    }

    fun toggleFavorite(item: FilmModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newStatus = repository.toggleFavorite(item)

                allFilms = allFilms.map {
                    if (it.id == item.id) it.copy(isFavorite = newStatus) else it
                }
                applyFilter()

                loadFavoritesFromDb()
            } catch (e: Exception) {
                _errorMessage.postValue(e.message ?: "Unknown error")
            }
        }
    }
}