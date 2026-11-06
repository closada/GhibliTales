package com.example.ghiblitales.data.ddbb

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.ghiblitales.data.FilmModel

class FilmDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ghibli.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_FAVORITES = "favorites"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_ORIGINAL_TITLE = "original_title"
        const val COLUMN_IMAGE = "image"
        const val COLUMN_MOVIE_BANNER = "movie_banner"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_DIRECTOR = "director"
        const val COLUMN_PRODUCER = "producer"
        const val COLUMN_RELEASE_DATE = "release_date"
        const val COLUMN_RUNNING_TIME = "running_time"
        const val COLUMN_RT_SCORE = "rt_score"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_FAVORITES (
            $COLUMN_ID TEXT PRIMARY KEY,
            $COLUMN_TITLE TEXT NOT NULL,
            $COLUMN_ORIGINAL_TITLE TEXT NOT NULL,
            $COLUMN_IMAGE TEXT NOT NULL,
            $COLUMN_MOVIE_BANNER TEXT NOT NULL,
            $COLUMN_DESCRIPTION TEXT NOT NULL,
            $COLUMN_DIRECTOR TEXT NOT NULL,
            $COLUMN_PRODUCER TEXT NOT NULL,
            $COLUMN_RELEASE_DATE TEXT NOT NULL,
            $COLUMN_RUNNING_TIME TEXT NOT NULL,
            $COLUMN_RT_SCORE TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    fun insertFavorites(item: FilmModel): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, item.id)
            put(COLUMN_TITLE, item.title)
            put(COLUMN_ORIGINAL_TITLE, item.originalTitle)
            put(COLUMN_IMAGE, item.image)
            put(COLUMN_MOVIE_BANNER, item.movieBanner)
            put(COLUMN_DESCRIPTION, item.description)
            put(COLUMN_DIRECTOR, item.director)
            put(COLUMN_PRODUCER, item.producer)
            put(COLUMN_RELEASE_DATE, item.releaseDate)
            put(COLUMN_RUNNING_TIME, item.runningTime)
            put(COLUMN_RT_SCORE, item.rtScore)
        }

        val result = db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
        return result != -1L
    }

    fun deleteFavorite(id: String): Boolean {
        val db = writableDatabase
        val result = db.delete(TABLE_FAVORITES, "$COLUMN_ID = ?", arrayOf(id))
        db.close()
        return result > 0
    }

    fun getAllFavorites(): List<FilmModel> {
        val list = mutableListOf<FilmModel>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FAVORITES", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val originalTitle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORIGINAL_TITLE))
                val image = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE))
                val movieBanner = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOVIE_BANNER))
                val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                val director = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIRECTOR))
                val producer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRODUCER))
                val releaseDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RELEASE_DATE))
                val runningTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RUNNING_TIME))
                val rtScore = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RT_SCORE))

                list.add(
                    FilmModel(
                        id = id,
                        title = title,
                        originalTitle = originalTitle,
                        image = image,
                        movieBanner = movieBanner,
                        description = description,
                        director = director,
                        producer = producer,
                        releaseDate = releaseDate,
                        runningTime = runningTime,
                        rtScore = rtScore,
                        isFavorite = true
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return list
    }

    fun isFavorite(id: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $TABLE_FAVORITES WHERE $COLUMN_ID = ?", arrayOf(id))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }
}