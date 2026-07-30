package com.xvantage.rental.ui.explore.discover.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ItemExploreListingCardBinding
import com.xvantage.rental.network.response.explore.ExploreListingResponse
import com.xvantage.rental.ui.explore.common.ExploreConstants
import com.xvantage.rental.ui.explore.common.ExploreUiMapper

class ExploreListingAdapter(
    private val context: Context,
    private val onItemClick: (ExploreListingResponse) -> Unit,
    private val onFavoriteClick: (ExploreListingResponse, Int) -> Unit
) : RecyclerView.Adapter<ExploreListingAdapter.ListingViewHolder>() {

    private var listings: List<ExploreListingResponse> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(newListings: List<ExploreListingResponse>) {
        this.listings = newListings
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun appendItems(moreListings: List<ExploreListingResponse>) {
        this.listings = this.listings + moreListings
        notifyDataSetChanged()
    }

    fun updateFavoriteState(position: Int, isFavorited: Boolean) {
        if (position !in listings.indices) return
        listings = listings.toMutableList().also {
            it[position] = it[position].copy(isFavorited = isFavorited)
        }
        notifyItemChanged(position)
    }

    inner class ListingViewHolder(private val binding: ItemExploreListingCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(listing: ExploreListingResponse, position: Int) {

            binding.tvTitle.text = listing.title
            binding.tvLocality.text = "${listing.locality}, ${listing.city}"
            binding.tvPriceRange.text = ExploreUiMapper.formatPriceRange(listing.priceRange)
            binding.chipCategory.text = listing.category?.name ?: "Listing"

            binding.imgFavorite.setImageResource(
                if (listing.isFavorited) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
            )

            val coverUrl = ExploreUiMapper.coverImageUrl(listing)
            if (coverUrl != null) {
                Glide.with(context)
                    .load(coverUrl)
                    .placeholder(R.drawable.ic_category_pg)
                    .error(R.drawable.ic_category_pg)
                    .into(binding.imgCover)
            } else {
                binding.imgCover.setImageResource(
                    ExploreConstants.categoryFallbackIcon(listing.category?.slug)
                )
            }

            binding.root.setOnClickListener { onItemClick(listing) }
            binding.imgFavorite.setOnClickListener { onFavoriteClick(listing, position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val binding = ItemExploreListingCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ListingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        holder.bind(listings[position], position)
    }

    override fun getItemCount(): Int = listings.size
}