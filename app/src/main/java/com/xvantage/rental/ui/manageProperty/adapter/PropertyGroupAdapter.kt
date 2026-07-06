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

    // Tracks which property positions are currently expanded (rooms visible).
    // Empty by default so all properties start collapsed.
    private val expandedPositions = mutableSetOf<Int>()

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

            property.property_room_no.forEach {

                android.util.Log.e(
                    "ROOM_DEBUG",
                    "Property=${property.name} Room=${it.room_no} Status=${it.status}"
                )
            }
            binding.tvTotalRooms.text =
                "Rooms : ${property.no_of_room}"


            val roomAdapter =
                ManagePropertyAdapter(
                    context,
                    listener
                )

            roomAdapter.addItems(
                property.property_room_no
            )

            binding.rvRooms.layoutManager =
                GridLayoutManager(context, 2)

            binding.rvRooms.setHasFixedSize(true)

            binding.rvRooms.adapter =
                roomAdapter

            // Restore correct expand/collapse state for this position
            // (RecyclerView recycles views, so this must be set on every bind).
            val isExpanded = expandedPositions.contains(adapterPosition)

            binding.detailsGroup.visibility =
                if (isExpanded) android.view.View.VISIBLE else android.view.View.GONE

            binding.ivExpandArrow.setImageResource(
                if (isExpanded) com.xvantage.rental.R.drawable.ic_up_arrow
                else com.xvantage.rental.R.drawable.ic_drop_down_arrow
            )

            val toggleExpand = {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (expandedPositions.contains(position)) {
                        expandedPositions.remove(position)
                    } else {
                        expandedPositions.add(position)
                    }
                    notifyItemChanged(position)
                }
            }

            binding.headerRow.setOnClickListener { toggleExpand() }
            binding.ivExpandArrow.setOnClickListener { toggleExpand() }

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