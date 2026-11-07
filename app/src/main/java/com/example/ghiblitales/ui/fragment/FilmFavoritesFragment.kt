package com.example.ghiblitales.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ghiblitales.databinding.FragmentFilmFavoritesBinding
import com.example.ghiblitales.ui.adapter.FilmAdapter
import com.example.ghiblitales.ui.detail.DetailActivity
import com.example.ghiblitales.ui.viewmodel.MainViewModel

/**
 * Favorites tab: lists the films saved in SQLite.
 * It reuses FilmAdapter and the same shared MainViewModel as the list Fragment.
 */
class FilmFavoritesFragment : Fragment() {

    // Same ViewBinding pattern as in FilmListFragment
    private var _binding: FragmentFilmFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: FilmAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedState: Bundle?
    ): View {
        _binding = FragmentFilmFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = FilmAdapter(
            onItemClick = { item ->
                startActivity(DetailActivity.newIntent(requireContext(), item))
            },
            // Pressing the star here removes the film from favorites; the list refreshes itself
            onFavoriteClick = { item ->
                viewModel.toggleFavorite(item)
            }
        )
        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavorites.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.favoritesList.observe(viewLifecycleOwner) { favorites ->
            adapter.submitList(favorites)
            // Empty state message when there are no favorites yet
            if (favorites.isNullOrEmpty()) {
                binding.tvEmptyFavorites.visibility = View.VISIBLE
            } else {
                binding.tvEmptyFavorites.visibility = View.GONE
            }
        }
    }

    // Reload on every return to this screen: favorites can change from the detail screen
    override fun onResume() {
        super.onResume()
        viewModel.loadFavoritesFromDb()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
