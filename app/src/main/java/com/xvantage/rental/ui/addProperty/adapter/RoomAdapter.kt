package com.xvantage.rental.ui.addProperty.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xvantage.rental.databinding.ItemRoomBinding
import com.xvantage.rental.ui.addProperty.tempFiles.Room
import java.text.NumberFormat
import java.util.Locale
import com.xvantage.rental.utils.UnitLabelProvider




/**
 * Project: Rental App By XV Team
 * Author: Mujammil x Vipul x XV Team
 * Date:  24/04/25
 * <p>
 * Licensed under the Apache License, Version 2.0. See LICENSE file for terms.
 */

class RoomAdapter(
    private val onEditClicked: (Room) -> Unit,
    private val onDeleteClicked: (Room) -> Unit,
) : ListAdapter<Room, RoomAdapter.RoomViewHolder>(RoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val binding = ItemRoomBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RoomViewHolder(private val binding: ItemRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(room: Room) {
            binding.tvRoomNumberCircle.text = room.number.take(2)
            binding.tvRoomName.text = "${UnitLabelProvider.forPropertyType(room.propertyTypeName).singular} ${room.number}"
            binding.tvRoomType.text = room.type.ifBlank { room.propertyTypeName }

            val formattedRent = NumberFormat.getCurrencyInstance(Locale.getDefault())
                .format(room.rent)
            binding.tvRoomRent.text = "$formattedRent/month"

            // Multi-bed (PG) rooms: show real occupancy ("2/4 Occupied") instead
            // of a flat Vacant/Occupied — otherwise it looks like a bug to the
            // owner when a room with tenants in it still says "Vacant".
            when {
                room.bedCount <= 1 -> {
                    binding.tvStatusBadge.text = if (room.isOccupied) "Occupied" else "Vacant"
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(
                        if (room.isOccupied) Color.parseColor("#4CAF50") else Color.parseColor("#FF9800")
                    )
                }
                room.occupiedBeds <= 0 -> {
                    binding.tvStatusBadge.text = "Vacant"
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF9800"))
                }
                room.occupiedBeds < room.bedCount -> {
                    binding.tvStatusBadge.text = "${room.occupiedBeds}/${room.bedCount} Occupied"
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                }
                else -> {
                    binding.tvStatusBadge.text = "Full"
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                }
            }

            binding.btnEditRoom.setOnClickListener {
                onEditClicked(room)
            }

            binding.btnDeleteRoom.setOnClickListener {
                onDeleteClicked(room)
            }
        }
    }

    private class RoomDiffCallback : DiffUtil.ItemCallback<Room>() {
        override fun areItemsTheSame(oldItem: Room, newItem: Room) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Room, newItem: Room) = oldItem == newItem
    }
}