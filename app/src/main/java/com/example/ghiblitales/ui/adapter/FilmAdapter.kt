package com.example.ghiblitales.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ghiblitales.data.FilmModel
import com.example.ghiblitales.databinding.ItemFilmBinding
import com.squareup.picasso.Picasso

class FilmAdapter(
    private val onItemClick: (FilmModel) -> Unit,
    private val onFavoriteClick: (FilmModel) -> Unit
) : ListAdapter<FilmModel, FilmAdapter.FilmViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FilmViewHolder {
        val binding = ItemFilmBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FilmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class FilmViewHolder(private val binding: ItemFilmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FilmModel) {
            binding.tvTitle.text = item.title
            binding.tvScore.text = "Score: ${item.rtScore}/100"
            binding.tvInfo.text = "${item.director} · ${item.releaseDate}"

            // carga de imagen con picasso (si la url viene vacía, Picasso lanzaría una excepción)
            if (item.image.isNotBlank()) {
                Picasso.get()
                    .load(item.image)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivFilm)
            } else {
                binding.ivFilm.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            val starDrawable = if (item.isFavorite) {
                android.R.drawable.btn_star_big_on
            } else {
                android.R.drawable.btn_star_big_off
            }
            binding.btnFavorite.setImageResource(starDrawable)

            binding.btnFavorite.setOnClickListener {
                onFavoriteClick(item)
            }

            // tocar la tarjeta abre el detalle
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    // cuando hay un cambio, no renderizar todo de cero sino solo los items que cambiaron
    companion object DiffCallback : DiffUtil.ItemCallback<FilmModel>() {
        override fun areItemsTheSame(oldItem: FilmModel, newItem: FilmModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FilmModel, newItem: FilmModel): Boolean {
            return oldItem == newItem
        }
    }
}