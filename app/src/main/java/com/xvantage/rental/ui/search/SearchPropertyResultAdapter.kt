package com.xvantage.rental.ui.search

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ItemPropertySearchResultBinding
import com.xvantage.rental.network.response.PropertyItem
import com.xvantage.rental.ui.addProperty.activity.PropertyDetailsActivity

class SearchPropertyResultAdapter(
    private val context: Context,
) : RecyclerView.Adapter<SearchPropertyResultAdapter.ViewHolder>() {

    private var propertiesList: List<PropertyItem> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(propertiesList: List<PropertyItem>) {
        this.propertiesList = propertiesList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val itemBinding: ItemPropertySearchResultBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        @SuppressLint("SetTextI18n")
        fun setData(data: PropertyItem) {
            itemBinding.tvPropertyName.text = data.name
            itemBinding.tvPropertyAddress.text = data.address
            itemBinding.roomsValue.text = "${data.property_room_no.size} Rooms"
            itemBinding.tenantsValue.text = "${data.total_tenants} Tenants"

            itemBinding.tvStatus.text = "Available"
            itemBinding.tvStatus.setBackgroundColor(
                ContextCompat.getColor(context, R.color.green)
            )

            if (data.property_images.isNotEmpty()) {
                Glide.with(context)
                    .load(data.property_images[0].image)
                    .placeholder(R.drawable.boarding_1)
                    .error(R.drawable.boarding_1)
                    .into(itemBinding.itemImage)
            } else {
                itemBinding.itemImage.setImageResource(R.drawable.boarding_1)
            }

            itemBinding.root.setOnClickListener {
                val intent = Intent(context, PropertyDetailsActivity::class.java)
                intent.putExtra("propertyId", data.id)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemPropertySearchResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(propertiesList[position])
    }

    override fun getItemCount(): Int = propertiesList.size
}