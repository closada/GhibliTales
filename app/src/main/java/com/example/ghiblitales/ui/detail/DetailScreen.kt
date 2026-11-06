package com.example.ghiblitales.ui.detail
import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ghiblitales.data.FilmModel
import com.example.ghiblitales.ui.theme.GhibliTalesTheme
import com.squareup.picasso.Picasso

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    film: FilmModel,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit,
) {
    // Scaffold: Material Design 3 base structure (TopBar, BottomBar, Content).
    Scaffold(
        // topBar: top bar with the back button.
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                // navigationIcon: left button; calls onBack to close the screen.
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { innerPadding -> // innerPadding: spacing given by Scaffold so content doesn't overlap the TopBar.
        // Column with verticalScroll: the synopsis can be long, so the screen scrolls.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // HD image (banner) of the film.
            FilmBanner(url = film.movieBanner)

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = film.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = film.originalTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline,
                )

                // Row: three quick-data cards, each one takes the same width (weight 1f).
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilmStat(label = "Score", value = "${film.rtScore}/100", modifier = Modifier.weight(1f))
                    FilmStat(label = "Release", value = film.releaseDate, modifier = Modifier.weight(1f))
                    FilmStat(label = "Runtime", value = "${film.runningTime} min", modifier = Modifier.weight(1f))
                }

                // Favorite button: the screen only reports the click, the ViewModel does the work.
                Button(
                    onClick = onToggleFavorite,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (isFavorite) "★ Remove from favorites" else "☆ Add to favorites")
                }

                // HorizontalDivider: Material Design horizontal dividing line.
                HorizontalDivider()

                InfoSection(title = "Synopsis", content = film.description)
                InfoSection(title = "Director", content = film.director)
                InfoSection(title = "Producer", content = film.producer)
            }
        }
    }
}

/**
 * Shows a network image with Picasso inside Compose.
 *
 * Compose has no built-in Picasso loader, so we use [AndroidView]:
 * it creates a traditional ImageView and Picasso loads the image into it.
 */
@Composable
private fun FilmBanner(url: String) {
    // In the Android Studio Preview Picasso is not initialized, so we skip loading there.
    val isPreview = LocalInspectionMode.current

    AndroidView(
        // factory: runs only once and creates the view.
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        // update: runs when the data changes; this is where the image is downloaded.
        update = { imageView ->
            if (!isPreview && url.isNotBlank()) {
                Picasso.get()
                    .load(url)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(imageView)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
    )
}

/**
 * Small card with a label and a value (score, release, runtime).
 */
@Composable
private fun FilmStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * Block with a section title and its content.
 */
@Composable
private fun InfoSection(title: String, content: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(text = content, style = MaterialTheme.typography.bodyLarge)
    }
}

/**
 * Compose preview to see the screen in Android Studio.
 */
@Preview(showBackground = true)
@Composable
private fun DetailScreenPreview() {
    GhibliTalesTheme {
        DetailScreen(
            film = FilmModel(
                id = "1",
                title = "Spirited Away",
                originalTitle = "千と千尋の神隠し",
                image = "",
                movieBanner = "",
                description = "A girl gets trapped in the world of spirits.",
                director = "Hayao Miyazaki",
                producer = "Toshio Suzuki",
                releaseDate = "2001",
                runningTime = "125",
                rtScore = "97",
            ),
            isFavorite = false,
            onToggleFavorite = {},
            onBack = {},
        )
    }
}