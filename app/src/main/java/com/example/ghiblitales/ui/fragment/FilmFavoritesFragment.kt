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

class FilmFavoritesFragment : Fragment() {

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
            if (favorites.isNullOrEmpty()) {
                binding.tvEmptyFavorites.visibility = View.VISIBLE
            } else {
                binding.tvEmptyFavorites.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavoritesFromDb()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}