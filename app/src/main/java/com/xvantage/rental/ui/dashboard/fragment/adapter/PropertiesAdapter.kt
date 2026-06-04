package com.xvantage.rental.ui.dashboard.fragment.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.xvantage.rental.R
import com.xvantage.rental.data.source.sample.Property
import com.xvantage.rental.databinding.HomePropertiesItemBinding
import com.xvantage.rental.utils.AppPreference
import android.content.Intent
import com.xvantage.rental.ui.addProperty.activity.PropertyDetailsActivity



class PropertiesAdapter(
    private val context: Context,
) : RecyclerView.Adapter<PropertiesAdapter.PropertyDetailsViewHolder>() {

    private lateinit var appPreference: AppPreference
    private var readImagePermission: String? = null
    private lateinit var propertiesList: List<Property>

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(propertiesList: List<Property>) {
        this.propertiesList = propertiesList
        notifyDataSetChanged()
    }


    inner class PropertyDetailsViewHolder(private val itemBinding: HomePropertiesItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        fun setData(data: Property, position: Int) {
            itemBinding.tvPropertyName.text = data.propertyName
            itemBinding.roomsValue.text = data.rooms.size.toString()
            itemBinding.tvPropertyAddress.text = data.address
            val totalTenants = data.rooms.count { it.tenant != null }
            itemBinding.tenantsValue.text = totalTenants.toString()

            if (position < data.rooms.size) {
                if (!data.rooms[position].occupied) {
                    itemBinding.tvStatus.setText("Vacant")
                    itemBinding.tvStatus.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.red
                        )
                    )
                } else {
                    itemBinding.tvStatus.setText("Occupied")
                    itemBinding.tvStatus.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.green
                        )
                    )
                }
            }
            itemBinding.moreButton.setOnClickListener {

                android.widget.Toast.makeText(
                    context,
                    "Opening ${data.propertyName}",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(
                    context,
                    PropertyDetailsActivity::class.java
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
