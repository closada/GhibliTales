package com.example.ghiblitales.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ghiblitales.databinding.FragmentFilmListBinding
import com.example.ghiblitales.ui.adapter.FilmAdapter
import com.example.ghiblitales.ui.detail.DetailActivity
import com.example.ghiblitales.ui.viewmodel.MainViewModel

/**
 * Main screen: search box + list of films.
 * The Fragment only shows data and reports user actions; the logic lives in the ViewModel.
 */
class FilmListFragment : Fragment() {

    // ViewBinding in a Fragment: the binding lives as long as the view, so it is nulled in onDestroyView
    private var _binding: FragmentFilmListBinding? = null
    private val binding get() = _binding!!

    // activityViewModels: the ViewModel belongs to the Activity, so it is shared with FilmFavoritesFragment
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: FilmAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedState: Bundle?
    ): View {
        _binding = FragmentFilmListBinding.inflate(inflater, container, false)
        return binding.root
    }

    // The view is ready: configure it and start observing the ViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observeViewModel()

        // Download the catalog only the first time, not every time we come back to this tab
        if (viewModel.filmList.value == null) {
            viewModel.loadFilms()
        }
    }

    private fun setupRecyclerView() {
        adapter = FilmAdapter(
            onItemClick = { item ->
                // Open the detail screen with the selected film
                startActivity(DetailActivity.newIntent(requireContext(), item))
            },
            onFavoriteClick = { item ->
                viewModel.toggleFavorite(item)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        // Restore the text the ViewModel remembers (for example when coming back from another tab)
        binding.etSearch.setText(viewModel.currentQuery)

        // Runs on every typed character: real-time search
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.searchFilms(text?.toString() ?: "")
        }
    }

    // Observer pattern: each time the LiveData changes, the UI updates itself
    private fun observeViewModel() {
        viewModel.filmList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isNullOrEmpty()) {
                binding.tvEmptyOrError.visibility = View.VISIBLE
            } else {
                binding.tvEmptyOrError.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Error messages thrown by the repository end up here, shown as a Toast
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // When we come back from the detail screen, the favorite stars may have changed there
    override fun onResume() {
        super.onResume()
        viewModel.syncFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
