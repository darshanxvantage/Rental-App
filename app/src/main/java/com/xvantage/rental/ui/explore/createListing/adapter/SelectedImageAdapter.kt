package com.xvantage.rental.ui.explore.createListing.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xvantage.rental.databinding.ItemSelectedImageBinding

class SelectedImageAdapter(
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<SelectedImageAdapter.ImageViewHolder>() {

    private var images: List<Uri> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newImages: List<Uri>) {
        this.images = newImages
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(private val binding: ItemSelectedImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri, position: Int) {
            Glide.with(binding.root.context)
                .load(uri)
                .centerCrop()
                .into(binding.imgSelectedThumbnail)

            binding.tvCoverBadge.visibility = if (position == 0) View.VISIBLE else View.GONE
            binding.btnRemoveSelectedImage.setOnClickListener { onRemoveClick(bindingAdapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemSelectedImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position], position)
    }

    override fun getItemCount(): Int = images.size
}