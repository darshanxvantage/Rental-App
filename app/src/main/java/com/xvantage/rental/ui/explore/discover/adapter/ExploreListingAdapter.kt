package com.xvantage.rental.ui.explore.discover.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
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
            binding.chipGender.text = ExploreUiMapper.genderLabel(listing.genderPreference)
            binding.chipFood.text = ExploreUiMapper.foodLabel(listing)

            binding.badgeVerified.visibility = if (listing.isVerified) android.view.View.VISIBLE else android.view.View.GONE
            binding.badgeFeatured.visibility = if (listing.isFeatured) android.view.View.VISIBLE else android.view.View.GONE

            val distanceText = ExploreUiMapper.formatDistance(listing.distanceKm)
            if (distanceText != null) {
                binding.tvDistance.visibility = android.view.View.VISIBLE
                binding.tvDistance.text = distanceText
            } else {
                binding.tvDistance.visibility = android.view.View.GONE
            }

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

            renderAmenityIcons(listing)

            binding.root.setOnClickListener { onItemClick(listing) }
            binding.imgFavorite.setOnClickListener { onFavoriteClick(listing, position) }
        }

        private fun renderAmenityIcons(listing: ExploreListingResponse) {
            val container = binding.layoutAmenities
            container.removeAllViews()

            val amenities = ExploreUiMapper.activeAmenityIcons(listing).take(5)
            val iconSizePx = (20 * context.resources.displayMetrics.density).toInt()
            val marginPx = (12 * context.resources.displayMetrics.density).toInt()

            amenities.forEach { (iconRes, _) ->
                val imageView = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(iconSizePx, iconSizePx).apply {
                        marginEnd = marginPx
                    }
                    setImageResource(iconRes)
                    ContextCompat.getColor(context, R.color.gray_sub).let { tintColor ->
                        imageTintList = android.content.res.ColorStateList.valueOf(tintColor)
                    }
                }
                container.addView(imageView)
            }
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