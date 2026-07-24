package com.xvantage.rental.ui.explore.favorites.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ItemFavoriteBinding
import com.xvantage.rental.network.response.explore.FavoriteEntryResponse
import com.xvantage.rental.ui.explore.common.ExploreUiMapper

class FavoriteAdapter(
    private val context: Context,
    private val onItemClick: (FavoriteEntryResponse) -> Unit,
    private val onRemoveClick: (FavoriteEntryResponse, Int) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    private var favorites: List<FavoriteEntryResponse> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newFavorites: List<FavoriteEntryResponse>) {
        this.favorites = newFavorites
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        if (position !in favorites.indices) return
        favorites = favorites.toMutableList().also { it.removeAt(position) }
        notifyItemRemoved(position)
    }

    inner class FavoriteViewHolder(private val binding: ItemFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(favorite: FavoriteEntryResponse, position: Int) {
            val listing = favorite.listing

            binding.tvFavoriteTitle.text = listing?.title ?: "Listing unavailable"
            binding.tvFavoriteLocality.text = listing?.let { "${it.locality}, ${it.city}" } ?: ""
            binding.tvFavoritePriceRange.text = ExploreUiMapper.formatPriceRange(listing?.priceRange)

            // if the listing was removed/deactivated by the owner since it was favorited
            val isUnavailable = listing == null || listing.status != "approved"
            binding.tvFavoriteStatus.visibility = if (isUnavailable) android.view.View.VISIBLE else android.view.View.GONE

            val coverUrl = listing?.let { ExploreUiMapper.coverImageUrl(it) }
            if (coverUrl != null) {
                Glide.with(context)
                    .load(coverUrl)
                    .placeholder(R.drawable.ic_category_pg)
                    .error(R.drawable.ic_category_pg)
                    .into(binding.imgFavoriteCover)
            } else {
                binding.imgFavoriteCover.setImageResource(R.drawable.ic_category_pg)
            }

            binding.root.setOnClickListener {
                listing?.let { onItemClick(favorite) }
            }
            binding.imgRemoveFavorite.setOnClickListener {
                onRemoveClick(favorite, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favorites[position], position)
    }

    override fun getItemCount(): Int = favorites.size
}