package com.xvantage.rental.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.xvantage.rental.R
import com.xvantage.rental.network.response.PropertyRoomItem

class RoomSpinnerAdapter(
    context: Context,
    private val rooms: List<PropertyRoomItem>
) : ArrayAdapter<PropertyRoomItem>(
    context,
    R.layout.item_room_spinner,
    rooms
) {

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        return createRow(position, convertView, parent)
    }

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        return createRow(position, convertView, parent)
    }

    private fun createRow(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {

        val view =
            convertView ?: LayoutInflater.from(context)
                .inflate(
                    R.layout.item_room_spinner,
                    parent,
                    false
                )

        val tvRoom =
            view.findViewById<TextView>(
                R.id.tvRoomName
            )

        val tvStatus =
            view.findViewById<TextView>(
                R.id.tvStatus
            )

        val room = rooms[position]

        tvRoom.text =
            "Room ${room.room_no}"

        if (room.status.equals("OCCUPED", true)) {

            tvStatus.text = "OCCUPIED"

            tvStatus.setBackgroundResource(
                R.drawable.green_status_bg
            )

        } else {

            tvStatus.text = "VACANT"

            tvStatus.setBackgroundResource(
                R.drawable.orange_status_bg
            )

        }

        return view
    }
}