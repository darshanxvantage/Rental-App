package com.xvantage.rental.ui.explore.details.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ItemSimilarListingBinding
import com.xvantage.rental.network.response.explore.ExploreListingResponse
import com.xvantage.rental.ui.explore.common.ExploreUiMapper

class SimilarListingAdapter(
    private val context: Context,
    private val onItemClick: (ExploreListingResponse) -> Unit
) : RecyclerView.Adapter<SimilarListingAdapter.SimilarViewHolder>() {

    private var listings: List<ExploreListingResponse> = emptyList()

    fun setItems(newListings: List<ExploreListingResponse>) {
        this.listings = newListings
        notifyDataSetChanged()
    }

    inner class SimilarViewHolder(private val binding: ItemSimilarListingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(listing: ExploreListingResponse) {
            binding.tvSimilarTitle.text = listing.title
            binding.tvSimilarLocality.text = listing.locality
            binding.tvSimilarPrice.text = ExploreUiMapper.formatPriceRange(listing.priceRange)

            val coverUrl = ExploreUiMapper.coverImageUrl(listing)
            if (coverUrl != null) {
                Glide.with(context)
                    .load(coverUrl)
                    .placeholder(R.drawable.ic_category_pg)
                    .error(R.drawable.ic_category_pg)
                    .into(binding.imgSimilarCover)
            } else {
                binding.imgSimilarCover.setImageResource(R.drawable.ic_category_pg)
            }

            binding.root.setOnClickListener { onItemClick(listing) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimilarViewHolder {
        val binding = ItemSimilarListingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SimilarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SimilarViewHolder, position: Int) {
        holder.bind(listings[position])
    }

    override fun getItemCount(): Int = listings.size
}