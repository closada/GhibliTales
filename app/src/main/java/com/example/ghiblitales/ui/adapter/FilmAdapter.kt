package com.example.ghiblitales.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ghiblitales.data.FilmModel
import com.example.ghiblitales.databinding.ItemFilmBinding
import com.squareup.picasso.Picasso

/**
 * Adapter of the RecyclerView: connects the list of films with the item_film.xml layout.
 * The same adapter is used in the film list and in the favorites list.
 *
 * It receives two lambdas, so the adapter only reports clicks and doesn't decide what to do with them
 * (the Fragment does).
 * ListAdapter + DiffUtil: when a new list arrives, only the items that changed are redrawn.
 */
class FilmAdapter(
    private val onItemClick: (FilmModel) -> Unit,
    private val onFavoriteClick: (FilmModel) -> Unit
) : ListAdapter<FilmModel, FilmAdapter.FilmViewHolder>(DiffCallback) {

    // Called only when a new row view is needed; RecyclerView then reuses those views while scrolling
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

    // Called every time a row has to show the data of the film at that position
    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    // ViewHolder: keeps the references to the views of one row (ViewBinding) so they are not searched again
    inner class FilmViewHolder(private val binding: ItemFilmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FilmModel) {
            binding.tvTitle.text = item.title
            binding.tvScore.text = "Score: ${item.rtScore}/100"
            binding.tvInfo.text = "${item.director} · ${item.releaseDate}"

            // Picasso downloads and shows the poster. An empty URL would make Picasso throw an exception,
            // so we use a default image in that case
            if (item.image.isNotBlank()) {
                Picasso.get()
                    .load(item.image)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(binding.ivFilm)
            } else {
                binding.ivFilm.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // The star icon depends on the favorite state of the film
            val starDrawable = if (item.isFavorite) {
                android.R.drawable.btn_star_big_on
            } else {
                android.R.drawable.btn_star_big_off
            }
            binding.btnFavorite.setImageResource(starDrawable)

            binding.btnFavorite.setOnClickListener {
                onFavoriteClick(item)
            }

            // Touching the card opens the detail screen
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    /**
     * DiffUtil compares the old and the new list to find out what changed.
     * areItemsTheSame: is it the same film? (same id)
     * areContentsTheSame: is the film's data the same? (data class equality)
     */
    companion object DiffCallback : DiffUtil.ItemCallback<FilmModel>() {
        override fun areItemsTheSame(oldItem: FilmModel, newItem: FilmModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FilmModel, newItem: FilmModel): Boolean {
            return oldItem == newItem
        }
    }
}
