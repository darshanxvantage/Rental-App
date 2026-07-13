package com.xvantage.rental.ui.dashboard.fragment.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xvantage.rental.R
import com.xvantage.rental.network.response.PropertyItem
import com.xvantage.rental.databinding.HomePropertiesItemBinding
import com.xvantage.rental.utils.AppPreference
import android.content.Intent
import com.xvantage.rental.ui.addProperty.activity.PropertyDetailsActivity
import android.util.Log
import com.bumptech.glide.Glide



class PropertiesAdapter(
    private val context: Context,
) : RecyclerView.Adapter<PropertiesAdapter.PropertyDetailsViewHolder>() {

    private lateinit var appPreference: AppPreference
    private var readImagePermission: String? = null
    private var propertiesList: List<PropertyItem> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(propertiesList: List<PropertyItem>) {
        this.propertiesList = propertiesList
        notifyDataSetChanged()
    }


    inner class PropertyDetailsViewHolder(private val itemBinding: HomePropertiesItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        @SuppressLint("SetTextI18n")
        fun setData(data: PropertyItem, position: Int) {
            Log.d(
                "PROPERTY_DEBUG",
                "Binding Property = ${data.name}"
            )

            itemBinding.tvPropertyName.text =
                data.name

            itemBinding.tvPropertyAddress.text =
                data.address

            if (data.property_images.isNotEmpty()) {

                Glide.with(context)
                    .load(data.property_images[0].image)
                    .placeholder(R.drawable.image)
                    .error(R.drawable.image)
                    .into(itemBinding.itemImage)

            } else {

                itemBinding.itemImage.setImageResource(
                    R.drawable.image
                )
            }

            itemBinding.roomsValue.text =
                data.property_room_no.size.toString()

            itemBinding.tenantsValue.text =
                data.total_tenants.toString()

            itemBinding.tvStatus.text =
                "Available"

            itemBinding.tvStatus.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.green
                )
            )

            itemBinding.moreButton.setOnClickListener {

//                android.widget.Toast.makeText(
//                    context,
//                    "Opening ${data.name}",
//                    android.widget.Toast.LENGTH_SHORT
//                ).show()

                val intent = Intent(
                    context,
                    PropertyDetailsActivity::class.java
                )

                intent.putExtra(
                    "propertyId",
                    data.id
                )

                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyDetailsViewHolder {
        val itemBinding = HomePropertiesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PropertyDetailsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: PropertyDetailsViewHolder, position: Int) {
        val data = propertiesList[position]
        appPreference = AppPreference(context)
        holder.setData(data, position)
    }

    override fun getItemCount(): Int = propertiesList.size
}
