package com.xvantage.rental.ui.explore.details.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ItemGalleryImageBinding

class ListingImagePagerAdapter(
    private val context: Context,
    private val imageUrls: List<String>
) : RecyclerView.Adapter<ListingImagePagerAdapter.GalleryViewHolder>() {

    inner class GalleryViewHolder(private val binding: ItemGalleryImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(url: String?) {
            if (url == null) {
                binding.imgGalleryPage.setImageResource(R.drawable.ic_category_pg)
                return
            }
            Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_category_pg)
                .error(R.drawable.ic_category_pg)
                .into(binding.imgGalleryPage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = ItemGalleryImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(imageUrls.getOrNull(position))
    }

    override fun getItemCount(): Int = if (imageUrls.isEmpty()) 1 else imageUrls.size
}