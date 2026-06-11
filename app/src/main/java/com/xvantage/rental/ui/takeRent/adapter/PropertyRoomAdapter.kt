package com.xvantage.rental.ui.takeRent.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ItemPropertyCardBinding
import com.xvantage.rental.databinding.ItemPropertyHeaderBinding
import com.xvantage.rental.ui.invoiceHistory.InvoiceHistoryActivity
import com.xvantage.rental.ui.takeRent.activity.ReceivePaymentActivity
import com.xvantage.rental.ui.takeRent.activity.TakeRentActivity

class PropertyRoomAdapter(
    private val propertyList: List<TakeRentActivity.PropertyItem>,
    private val context: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PROPERTY = 0
        private const val TYPE_ROOM = 1
    }

    private val expandedProperties = propertyList.map { it.propertyName }.toMutableSet()

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TakeRentActivity.PropertyItem -> TYPE_PROPERTY
            is TakeRentActivity.RoomItem -> TYPE_ROOM
            else -> throw IllegalArgumentException("Invalid data type at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_PROPERTY -> {
                val binding = ItemPropertyHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PropertyViewHolder(binding)
            }
            TYPE_ROOM -> {
                val binding = ItemPropertyCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                RoomViewHolder(binding, context)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TakeRentActivity.PropertyItem -> (holder as PropertyViewHolder).bind(item)
            is TakeRentActivity.RoomItem -> (holder as RoomViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = getDisplayList().size

    private fun getItem(position: Int): Any = getDisplayList()[position]

    inner class PropertyViewHolder(private val binding: ItemPropertyHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(property: TakeRentActivity.PropertyItem) {
            binding.propertyTitle.text = property.propertyName
            val isExpanded = expandedProperties.contains(property.propertyName)
            binding.propertyTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, if (isExpanded) R.drawable.ic_up_arrow else R.drawable.ic_drop_down_arrow, 0)

            binding.root.setOnClickListener {
                if (isExpanded) {
                    expandedProperties.remove(property.propertyName)
                } else {
                    expandedProperties.add(property.propertyName)
                }
                notifyDataSetChanged()
            }
        }
    }

    inner class RoomViewHolder(private val binding: ItemPropertyCardBinding, private val context: Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(room: TakeRentActivity.RoomItem) {
            android.util.Log.e(
                "ROOM_UI",
                "Room=${room.roomId} Occupied=${room.occupied}"
            )
            binding.roomId.text = room.roomId
            binding.address.text = room.address

            if (room.occupied) {

                binding.tvStatus.text = "Occupied"

                binding.tvStatus.setBackgroundResource(
                    R.drawable.green_status_bg
                )

            } else {

                binding.tvStatus.text = "Vacant"

                binding.tvStatus.setBackgroundResource(
                    R.drawable.orange_status_bg
                )
            }
            binding.monthlyRent.text = "₹${room.monthlyRent}"
            binding.tvDepositAmount.text = "₹${room.securityAmount}"

            binding.btnRcvPayment.setOnClickListener {
                startActivity(context, Intent(context, ReceivePaymentActivity::class.java), null)
            }
            binding.btnGenerateInvoice.setOnClickListener {
                startActivity(context, Intent(context, InvoiceHistoryActivity::class.java), null)
            }
        }
    }

    private fun getDisplayList(): List<Any> {
        val displayList = mutableListOf<Any>()
        for (property in propertyList) {
            displayList.add(property)
            if (expandedProperties.contains(property.propertyName)) {
                displayList.addAll(property.rooms)
            }
        }
        return displayList
    }
}
