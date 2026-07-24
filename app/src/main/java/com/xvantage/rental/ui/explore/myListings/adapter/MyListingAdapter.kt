package com.xvantage.rental.ui.explore.myListings.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ItemMyListingBinding
import com.xvantage.rental.network.response.explore.ExploreListingResponse
import com.xvantage.rental.ui.explore.common.ExploreUiMapper

class MyListingAdapter(
    private val context: Context,
    private val onItemClick: (ExploreListingResponse) -> Unit,
    private val onMenuClick: (ExploreListingResponse, android.view.View) -> Unit
) : RecyclerView.Adapter<MyListingAdapter.MyListingViewHolder>() {

    private var listings: List<ExploreListingResponse> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newListings: List<ExploreListingResponse>) {
        this.listings = newListings
        notifyDataSetChanged()
    }

    inner class MyListingViewHolder(private val binding: ItemMyListingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(listing: ExploreListingResponse) {
            binding.tvMyListingTitle.text = listing.title
            binding.tvMyListingLocality.text = "${listing.locality}, ${listing.city}"
            binding.tvMyListingPrice.text = ExploreUiMapper.formatPriceRange(listing.priceRange)
            binding.tvMyListingStatus.text = ExploreUiMapper.statusLabel(listing.status)
            binding.tvMyListingStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(context, ExploreUiMapper.statusColorRes(listing.status))
            )
            binding.tvMyListingStats.text = "${listing.viewCount} views • ${listing.leadCount} leads"

            val coverUrl = ExploreUiMapper.coverImageUrl(listing)
            if (coverUrl != null) {
                Glide.with(context)
                    .load(coverUrl)
                    .placeholder(R.drawable.ic_category_pg)
                    .error(R.drawable.ic_category_pg)
                    .into(binding.imgMyListingCover)
            } else {
                binding.imgMyListingCover.setImageResource(R.drawable.ic_category_pg)
            }

            binding.root.setOnClickListener { onItemClick(listing) }
            binding.imgMyListingMenu.setOnClickListener { onMenuClick(listing, it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyListingViewHolder {
        val binding = ItemMyListingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyListingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyListingViewHolder, position: Int) {
        holder.bind(listings[position])
    }

    override fun getItemCount(): Int = listings.size
}