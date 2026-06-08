package com.xvantage.rental.ui.manageProperty.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xvantage.rental.databinding.ItemPropertyGroupBinding
import com.xvantage.rental.network.response.PropertyItem

class PropertyGroupAdapter(
    private val context: Context,
    private val listener: ManagePropertyAdapter.OnRoomItemClickListener,
    private val propertyListener: PropertyActionListener
) : RecyclerView.Adapter<PropertyGroupAdapter.ViewHolder>() {

    private var propertyList: List<PropertyItem> = emptyList()

    interface PropertyActionListener {

        fun onEditProperty(
            property: PropertyItem
        )

        fun onDeleteProperty(
            property: PropertyItem
        )
    }

    fun addItems(list: List<PropertyItem>) {
        propertyList = list
        notifyDataSetChanged()
    }



    inner class ViewHolder(
        private val binding: ItemPropertyGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(property: PropertyItem) {

            binding.tvPropertyName.text =
                property.name

            binding.tvPropertyAddress.text =
                property.address

            binding.tvTotalRooms.text =
                "Rooms : ${property.no_of_room}"


            val roomAdapter =
                ManagePropertyAdapter(
                    context,
                    listener
                )

            roomAdapter.addItems(
                property.property_room_no.map {
                    it.room_no
                }
            )

            binding.rvRooms.layoutManager =
                GridLayoutManager(context, 2)

            binding.rvRooms.setHasFixedSize(true)

            binding.rvRooms.adapter =
                roomAdapter

            binding.ivEdit.setOnClickListener {

                propertyListener.onEditProperty(
                    property
                )
            }

            binding.ivDelete.setOnClickListener {

                propertyListener.onDeleteProperty(
                    property
                )
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        return ViewHolder(
            ItemPropertyGroupBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(propertyList[position])
    }

    override fun getItemCount() =
        propertyList.size
}