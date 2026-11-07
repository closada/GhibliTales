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

/**
 * ViewModel shared by the two Fragments (film list and favorites).
 *
 * It holds the UI state and survives screen rotations. The Fragments only observe the LiveData
 * exposed here; they don't know where the data comes from.
 * AndroidViewModel is used because the database helper needs the application Context.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FilmRepository(FilmDbHelper(application))

    // Full list downloaded from the API (or the saved favorites when there is no connection)
    private var allFilms: List<FilmModel> = emptyList()

    // Current text of the search box. Readable from outside, but only this class can change it
    var currentQuery: String = ""
        private set

    // Pattern: a private MutableLiveData to write, and a public read-only LiveData to observe.
    // This way the UI cannot modify the state by mistake.
    // filmList is what the screen draws: allFilms already filtered by currentQuery
    private val _filmList = MutableLiveData<List<FilmModel>>()
    val filmList: LiveData<List<FilmModel>> = _filmList

    private val _favoritesList = MutableLiveData<List<FilmModel>>()
    val favoritesList: LiveData<List<FilmModel>> = _favoritesList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Loads the catalog. The coroutine runs on Dispatchers.IO (background thread)
     * because network and database work must not block the main thread.
     * postValue() is used because we are NOT on the main thread.
     */
    fun loadFilms() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            _errorMessage.postValue(null)
            try {
                allFilms = repository.fetchFilms()
            } catch (e: Exception) {
                // The repository already built a readable message for the user
                _errorMessage.postValue(e.message ?: "Unknown error")
                // Offline mode: show what is saved in SQLite
                allFilms = repository.getFavorites()
            } finally {
                // Runs in both cases (success or error)
                applyFilter()
                _isLoading.postValue(false)
            }
        }
    }

    // Real-time search: it works on the list already in memory, so it needs no new API call
    // and no background thread (filtering a few items is instant)
    fun searchFilms(query: String) {
        currentQuery = query
        applyFilter()
    }

    // Publishes allFilms filtered by the current query (or the whole list if the query is empty)
    private fun applyFilter() {
        val query = currentQuery.trim()
        val filtered = if (query.isEmpty()) {
            allFilms
        } else {
            allFilms.filter { it.title.contains(query, ignoreCase = true) }
        }
        _filmList.postValue(filtered)
    }

    // Reads the favorites table (used by the Favorites tab)
    fun loadFavoritesFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getFavorites()
            _favoritesList.postValue(list)
        }
    }

    // Re-reads the favorite flags from SQLite (the detail screen can change them)
    fun syncFavorites() {
        if (allFilms.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            allFilms = allFilms.map { it.copy(isFavorite = repository.isFavorite(it.id)) }
            applyFilter()
        }
    }

    /**
     * Adds or removes a film from favorites and refreshes both lists.
     * We create a copy with copy() instead of modifying the old object: the adapter's DiffUtil
     * compares old and new items, and it would not notice a change on the same instance.
     */
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
