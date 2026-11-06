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

class FilmListFragment : Fragment() {

    private var _binding: FragmentFilmListBinding? = null
    private val binding get() = _binding!!

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        observeViewModel()

        if (viewModel.filmList.value == null) {
            viewModel.loadFilms()
        }
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
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        // restauramos el texto que el ViewModel recuerda (por ejemplo al volver de otra pestaña)
        binding.etSearch.setText(viewModel.currentQuery)

        // se ejecuta en cada letra que escribe el usuario
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.searchFilms(text?.toString() ?: "")
        }
    }

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

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncFavorites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}