package com.example.ghiblitales

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.ghiblitales.databinding.ActivityMainBinding
import com.example.ghiblitales.ui.fragment.FilmFavoritesFragment
import com.example.ghiblitales.ui.fragment.FilmListFragment

/**
 * Entry point of the app. It has almost no logic: it only hosts the two Fragments
 * and switches between them with the bottom navigation bar.
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding: typed access to the views of activity_main.xml (no findViewById)
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Edge-to-edge: leave room for the status bar so the content is not hidden behind it
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Show the film list only on the first launch (after a rotation the Fragment is restored by Android)
        if (savedInstanceState == null) {
            replaceFragment(FilmListFragment())
        }

        // Bottom navigation: each tab shows a different Fragment
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_films -> {
                    replaceFragment(FilmListFragment())
                    true
                }
                R.id.nav_favorites -> {
                    replaceFragment(FilmFavoritesFragment())
                    true
                }
                else -> false
            }
        }
    }

    // Swaps the Fragment shown inside the FragmentContainerView
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
