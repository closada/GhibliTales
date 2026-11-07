# Ghibli Tales 🎬

Android app to explore the Studio Ghibli film catalog, search films, see their details and save favorites to read them offline.

## Demo video

[Watch the demo video on Google Drive](https://drive.google.com/file/d/1yJq7sYcDxpzlvuXUG4n5_MtwCuUv9a07/view?usp=sharing)

## Features

| Requirement | Implementation |
|---|---|
| **Main screen** (Fragment + XML + RecyclerView) | `FilmListFragment` shows every film with its cover (Picasso), title and score (Rotten Tomatoes score from the API). |
| **Real-time search** | A search box filters the list by title on every typed character. |
| **Detail screen** (Jetpack Compose) | `DetailActivity` + `DetailScreen`: HD banner, original title, score, release year, runtime, synopsis, director and producer. |
| **Favorites** (SQLite) | Star button on each list item **and** a button on the detail screen. Favorites are stored in a local SQLite database and listed in the *Favorites* tab. |
| **Offline mode** | Favorites store every film field, so they can be opened without internet. If the network fails, the app shows the saved favorites. |

## API

[Studio Ghibli API](https://ghibliapi.vercel.app/) (no API key needed).

- `GET /films` → the whole catalog in a single request. It already includes everything the detail screen needs, so no second endpoint is used.

**Notes about the API:**
- It has no search endpoint, so the search is done **locally**: the ViewModel keeps the full list in memory and filters it by title.
- It has no genres, episodes or broadcast status. The detail screen shows the data the API does provide: director, producer, runtime, release year and score.
- The API was provided by the instructor as a replacement for Jikan API (which was down).

## Architecture

The app follows **MVVM** with a **Repository**:

```
Fragment / Activity (View)
        │  observes LiveData / StateFlow
        ▼
ViewModel  (MainViewModel, DetailViewModel)
        │
        ▼
FilmRepository
   ├── Retrofit + Gson  →  Studio Ghibli API
   └── FilmDbHelper (SQLiteOpenHelper) →  local favorites
```

- **View:** Fragments with XML + ViewBinding (list and favorites) and Jetpack Compose (detail). Views only display data and report user actions.
- **ViewModel:** holds the UI state (film list, favorites, loading, error, search query).
- **Repository:** decides where data comes from (network or database) and throws readable exceptions.

### Design patterns

- **MVVM** (required architecture).
- **Repository:** hides the data sources from the ViewModels.
- **Singleton:** `RetrofitClient` is a Kotlin `object` created lazily.
- **Observer:** `LiveData` and `StateFlow`.
- **Constructor dependency injection:** `FilmRepository` receives its `FilmDbHelper`.
- **Factory method:** `DetailActivity.newIntent(...)` builds the Intent in a single place.

### Error handling

- `FilmRepository` translates technical errors into readable messages using `throw Exception(...)`:
    - `HttpException` → "Server error (code …)"
    - `IOException` → "No internet connection or the request timed out"
    - empty API response → "The API returned no films"
    - SQLite failures when saving or removing a favorite.
- The ViewModels catch those exceptions with `try/catch` and expose the message; the UI shows it in a `Toast`.
- Loading state with a `ProgressBar` and empty-state messages for the list and the favorites.
- If the film does not reach the detail screen, the user is warned and the screen closes.
- Empty image URLs are checked before calling Picasso.

## Tech stack

- Kotlin, Android Studio
- Retrofit 2 + Gson converter
- Picasso (images, also used inside Compose through `AndroidView`)
- Jetpack Compose + Material 3 (detail screen)
- RecyclerView + ViewBinding (list screens)
- SQLite (`SQLiteOpenHelper`)
- Kotlin Coroutines, LiveData, StateFlow, Fragment KTX

## Project structure

```
com.example.ghiblitales
├── MainActivity.kt
├── data
│   ├── FilmModel.kt
│   ├── ddbb/FilmDbHelper.kt
│   ├── remote/ApiService.kt
│   ├── remote/RetrofitClient.kt
│   └── repository/FilmRepository.kt
└── ui
    ├── adapter/FilmAdapter.kt
    ├── fragment/FilmListFragment.kt
    ├── fragment/FilmFavoritesFragment.kt
    ├── viewmodel/MainViewModel.kt
    ├── viewmodel/DetailViewModel.kt
    ├── detail/DetailActivity.kt
    ├── detail/DetailScreen.kt
    └── theme/ (Color.kt, Type.kt, Theme.kt)
```

## How to run

1. Clone the repository:
   ```
   git clone https://github.com/closada/GhibliTales.git
   ```
2. Open it with Android Studio and wait for the Gradle sync to finish.
3. Run the `app` module on an emulator or a physical device.

An internet connection is needed the first time to download the catalog. No API key is required.

## Author

Camila Losada([@closada](https://github.com/closada))