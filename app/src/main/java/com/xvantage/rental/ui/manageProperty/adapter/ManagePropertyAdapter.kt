package com.xvantage.rental.ui.manageProperty.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xvantage.rental.databinding.PropertyRoomItemsBinding
import com.xvantage.rental.network.response.PropertyRoom
import com.xvantage.rental.R
import android.annotation.SuppressLint


class ManagePropertyAdapter(
    private val context: Context,
    private val listener: OnRoomItemClickListener,
    private val propertyTypeName: String? = null
) : RecyclerView.Adapter<ManagePropertyAdapter.ManagePropertyViewHolder>() {

    private var roomList: List<PropertyRoom> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(roomList: List<PropertyRoom>) {

        this.roomList = roomList

        notifyDataSetChanged()
    }

    interface OnRoomItemClickListener {
        fun onRoomClick(roomNumber: String, position: Int)
        fun onAddTenantClick(room: PropertyRoom, position: Int, propertyTypeName: String?)
    }

    inner class ManagePropertyViewHolder(
        private val itemBinding: PropertyRoomItemsBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(
            room: PropertyRoom,
            position: Int
        ) {

            itemBinding.tvRoomNumber.text =
                room.room_no

            val bedCount = if (room.bedCount > 0) room.bedCount else 1
            val isFull = if (bedCount <= 1) {
                room.status.contains("OCCUP", true)
            } else {
                room.occupiedBeds >= bedCount
            }

            when {
                bedCount <= 1 -> {
                    itemBinding.tvOccupied.text = if (isFull) "Occupied" else "Vacant"
                    itemBinding.tvOccupied.setBackgroundResource(
                        if (isFull) R.drawable.green_status_bg else R.drawable.orange_status_bg
                    )
                }
                room.occupiedBeds <= 0 -> {
                    itemBinding.tvOccupied.text = "Vacant"
                    itemBinding.tvOccupied.setBackgroundResource(R.drawable.orange_status_bg)
                }
                isFull -> {
                    itemBinding.tvOccupied.text = "Full"
                    itemBinding.tvOccupied.setBackgroundResource(R.drawable.green_status_bg)
                }
                else -> {
                    itemBinding.tvOccupied.text = "${room.occupiedBeds}/$bedCount Occupied"
                    itemBinding.tvOccupied.setBackgroundResource(R.drawable.orange_status_bg)
                }
            }

            itemBinding.btnAddTenant.text = if (isFull) "View Tenant" else "Add Tenant"

            itemBinding.root.setOnClickListener {

                listener.onRoomClick(
                    room.room_no,
                    position
                )
            }

            itemBinding.btnAddTenant.setOnClickListener {

                listener.onAddTenantClick(
                    room,
                    position,
                    propertyTypeName
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManagePropertyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = PropertyRoomItemsBinding.inflate(inflater, parent, false)
        return ManagePropertyViewHolder(itemBinding)
    }
    override fun onBindViewHolder(
        holder: ManagePropertyViewHolder,
        position: Int
    ) {

        holder.bind(
            roomList[position],
            position
        )
    }

    override fun getItemCount(): Int = roomList.size
}