package com.xvantage.rental.ui.takeRent.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ItemPropertyCardBinding
import com.xvantage.rental.databinding.ItemPropertyHeaderBinding
import com.xvantage.rental.ui.takeRent.bmsheet.ReceivePaymentBottomSheetFragment
import com.xvantage.rental.ui.takeRent.activity.TakeRentActivity
import java.text.NumberFormat
import java.util.Locale

class PropertyRoomAdapter(
    private val propertyList: List<TakeRentActivity.PropertyItem>,
    private val context: Context,
    private val onGenerateInvoice: (tenantId: String) -> Unit = {},
    private val onPaymentReceived: () -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PROPERTY = 0
        private const val TYPE_ROOM     = 1
    }


    private val expandedProperties =
        propertyList.map { it.propertyId }.toMutableSet()

    override fun getItemViewType(position: Int): Int {
        return when (getDisplayList()[position]) {
            is TakeRentActivity.PropertyItem -> TYPE_PROPERTY
            is TakeRentActivity.RoomItem     -> TYPE_ROOM
            else -> throw IllegalArgumentException("Invalid type at $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_PROPERTY -> {
                val binding = ItemPropertyHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
                PropertyViewHolder(binding)
            }
            TYPE_ROOM -> {
                val binding = ItemPropertyCardBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
                RoomViewHolder(binding, context)
            }


            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getDisplayList()[position]) {
            is TakeRentActivity.PropertyItem -> (holder as PropertyViewHolder).bind(item)
            is TakeRentActivity.RoomItem     -> (holder as RoomViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = getDisplayList().size

    inner class PropertyViewHolder(
        private val binding: ItemPropertyHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(property: TakeRentActivity.PropertyItem) {
            binding.propertyTitle.text = property.propertyName

            val isExpanded = expandedProperties.contains(property.propertyId)
            binding.propertyTitle.setCompoundDrawablesWithIntrinsicBounds(
                0, 0,
                if (isExpanded) R.drawable.ic_up_arrow else R.drawable.ic_drop_down_arrow,
                0
            )

            binding.root.setOnClickListener {
                if (isExpanded) expandedProperties.remove(property.propertyId)
                else expandedProperties.add(property.propertyId)
                notifyDataSetChanged()
            }
        }
    }

    private val currencyFormatter =
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
    inner class RoomViewHolder(
        private val binding: ItemPropertyCardBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {


        private var totalPayableForRoom: Double = 0.0

        fun bind(room: TakeRentActivity.RoomItem) {


            android.util.Log.e(
                "ROOM_IMAGE",
                """
Room Id     = ${room.roomId}
Tenant Name = ${room.tenantName}
Profile Pic = ${room.profilePic}
Occupied    = ${room.isOccupied}
""".trimIndent()
            )


            binding.roomId.text = "Room ${room.roomNo}"
            binding.address.text = room.propertyName


            if (room.isOccupied) {
                binding.tvStatus.text = "Occupied"
                binding.tvStatus.setBackgroundResource(R.drawable.green_status_bg)
            } else {
                binding.tvStatus.text = "Vacant"
                binding.tvStatus.setBackgroundResource(R.drawable.orange_status_bg)
            }


            binding.tvTenantName.text = room.tenantName

            if (room.isOccupied && room.phone.isNotEmpty()) {
                binding.tvPhone.text       = room.phone
                binding.tvPhone.visibility = View.VISIBLE
            } else {
                binding.tvPhone.visibility = View.GONE
            }


            if (room.profilePic.isNotEmpty()) {
                Glide.with(context)
                    .load(room.profilePic)
                    .placeholder(R.drawable.permisson_img)
                    .error(R.drawable.permisson_img)
                    .circleCrop()
                    .into(binding.roomImage)
            } else {
                Glide.with(context)
                    .load(R.drawable.permisson_img)
                    .circleCrop()
                    .into(binding.roomImage)
            }


            if (room.isOccupied) {

                // ── Monthly Rent — sirf base rent dikhao (electricity/water alag) ──
                binding.monthlyRent.text =
                    if (room.monthlyRent > 0)
                        "₹${room.monthlyRent.toLong()}"
                    else
                        "--"

                binding.tvDepositAmount.text =
                    if (room.securityAmount > 0)
                        currencyFormatter.format(room.securityAmount)
                    else
                        "--"

                binding.rentStartDate.text =
                    room.rentStartDate.ifEmpty { "--" }

                binding.rentDepositTill.text =
                    room.rentSettledTill.ifEmpty { "--" }

                binding.nextDueDate.text =
                    room.nextDueDate.ifEmpty { "--" }

                binding.tvAdvance.text =
                    currencyFormatter.format(room.advance)

                // ── Payment Due from billing_cycles (accurate) ──
                binding.paymentDue.text =
                    if (room.paymentDue > 0)
                        "₹${room.paymentDue.toLong()}"
                    else
                        "₹0"

                // ── Electricity info ──
                val elecText = when (room.electricityMode.lowercase().trim()) {
                    "fix", "fixed" -> "Fixed  ₹${room.fixedElectricity.toLong()}"
                    "meter", "metered" -> "Metered (per use)"
                    else -> "No Cost"
                }
                binding.tvElectricityInfo.text = elecText

                // ── Water info ──
                val waterText = when (room.waterMode.lowercase().trim()) {
                    "fix", "fixed" -> "Fixed  ₹${room.fixedWater.toLong()}"
                    "meter", "metered" -> "Metered (per use)"
                    else -> "No Cost"
                }
                binding.tvWaterInfo.text = waterText




                var total = room.monthlyRent


                if (
                    room.electricityMode.equals("fixed", true) ||
                    room.electricityMode.equals("fix", true)
                ) {
                    total += room.fixedElectricity
                }


                if (
                    room.waterMode.equals("fixed", true) ||
                    room.waterMode.equals("fix", true)
                ) {
                    total += room.fixedWater
                }

                binding.tvTotalPayableCard.text =
                    currencyFormatter.format(total)


                totalPayableForRoom = total
            }else {

                binding.monthlyRent.text     = "N/A"
                binding.tvDepositAmount.text = "N/A"
                binding.rentStartDate.text   = "N/A"
                binding.rentDepositTill.text = "N/A"
                binding.nextDueDate.text     = "N/A"
                binding.tvAdvance.text       = "N/A"
                binding.paymentDue.text      = "N/A"
                binding.tvElectricityInfo.text = "N/A"
                binding.tvWaterInfo.text       = "N/A"
                binding.tvTotalPayableCard.text = "N/A"
            }

            if (room.isOccupied && room.phone.isNotEmpty()) {
                binding.whatsappIcon.visibility = View.VISIBLE
                binding.callIcon.visibility     = View.VISIBLE

                binding.whatsappIcon.setOnClickListener {
                    val url = "https://wa.me/91${room.phone}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
                binding.callIcon.setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${room.phone}"))
                    context.startActivity(intent)
                }
            } else {
                binding.whatsappIcon.visibility = View.INVISIBLE
                binding.callIcon.visibility     = View.INVISIBLE
            }

            if (room.isOccupied) {

                binding.btnRcvPayment.isEnabled = true
                binding.btnRcvPayment.alpha     = 1f
                binding.btnGenerateInvoice.isEnabled = true
                binding.btnGenerateInvoice.alpha     = 1f

                binding.btnRcvPayment.setOnClickListener {

                    val sheet = ReceivePaymentBottomSheetFragment.newInstance(
                        tenantId = room.tenantId,
                        tenantName = room.tenantName,
                        roomId = room.roomId,
                        propertyName = room.propertyName,
                        totalPayable = totalPayableForRoom,
                        electricityMode = room.electricityMode,
                        waterMode = room.waterMode,
                        lastMeterReading = room.lastMeterReading,
                        lastWaterReading = room.lastWaterReading,
                        costPerUnit = room.costPerUnit,
                        costUnitWater = room.costUnitWater
                    )

                    sheet.setOnPaymentReceivedListener {
                        onPaymentReceived()
                    }

                    (context as? androidx.fragment.app.FragmentActivity)
                        ?.supportFragmentManager
                        ?.let { fm -> sheet.show(fm, "ReceivePayment") }
                }

                binding.btnGenerateInvoice.setOnClickListener {
                    if (room.tenantId.isNotBlank()) {
                        onGenerateInvoice(room.tenantId)
                    }
                }
            } else {
                // Vacant room — disable buttons
                binding.btnRcvPayment.isEnabled      = false
                binding.btnRcvPayment.alpha           = 0.4f
                binding.btnGenerateInvoice.isEnabled  = false
                binding.btnGenerateInvoice.alpha      = 0.4f
            }
        }
    }

    private fun getDisplayList(): List<Any> {
        val list = mutableListOf<Any>()
        for (property in propertyList) {
            list.add(property)
            if (expandedProperties.contains(property.propertyId)) {
                list.addAll(property.rooms)
            }
        }
        return list
    }
}