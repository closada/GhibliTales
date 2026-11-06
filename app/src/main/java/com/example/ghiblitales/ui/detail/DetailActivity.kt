package com.example.ghiblitales.ui.detail
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.IntentCompat
import com.example.ghiblitales.data.FilmModel
import com.example.ghiblitales.ui.theme.GhibliTalesTheme
import com.example.ghiblitales.ui.viewmodel.DetailViewModel

class DetailActivity : ComponentActivity() {

    private val viewModel: DetailViewModel by viewModels()

    companion object {
        private const val EXTRA_FILM = "extra_film"

        // Single place to build the Intent, so callers don't need to know the extra key
        fun newIntent(context: Context, film: FilmModel): Intent {
            return Intent(context, DetailActivity::class.java).apply {
                putExtra(EXTRA_FILM, film)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val film = IntentCompat.getSerializableExtra(intent, EXTRA_FILM, FilmModel::class.java)

        // If the film did not arrive for some reason, warn the user and close the screen
        if (film == null) {
            Toast.makeText(this, "Could not load the film", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.loadFilm(film)

        setContent {
            GhibliTalesTheme {
                // collectAsState: subscribes to the StateFlow and recomposes when the value changes
                val isFavorite by viewModel.isFavorite.collectAsState()
                val error by viewModel.errorMessage.collectAsState()

                // Shows the error (if any) once and then clears it
                LaunchedEffect(error) {
                    error?.let {
                        Toast.makeText(this@DetailActivity, it, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }

                DetailScreen(
                    film = film,
                    isFavorite = isFavorite,
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    onBack = { finish() },
                )
            }
        }
    }
}