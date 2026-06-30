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
    private val listener: OnRoomItemClickListener
) : RecyclerView.Adapter<ManagePropertyAdapter.ManagePropertyViewHolder>() {

    private var roomList: List<PropertyRoom> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(roomList: List<PropertyRoom>) {

        this.roomList = roomList

        notifyDataSetChanged()
    }

    interface OnRoomItemClickListener {
        fun onRoomClick(roomNumber: String, position: Int)
        fun onAddTenantClick(room: PropertyRoom, position: Int)
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

            if (
                room.status.contains(
                    "OCCUP",
                    true
                )
            ) {

                itemBinding.tvOccupied.text =
                    "Occupied"

                itemBinding.tvOccupied.setBackgroundResource(
                    R.drawable.green_status_bg
                )

                itemBinding.btnAddTenant.text =
                    "View Tenant"

            } else {

                itemBinding.tvOccupied.text =
                    "Vacant"

                itemBinding.tvOccupied.setBackgroundResource(
                    R.drawable.orange_status_bg
                )

                itemBinding.btnAddTenant.text =
                    "Add Tenant"
            }

            itemBinding.root.setOnClickListener {

                listener.onRoomClick(
                    room.room_no,
                    position
                )
            }

            itemBinding.btnAddTenant.setOnClickListener {

                listener.onAddTenantClick(
                    room,
                    position
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
