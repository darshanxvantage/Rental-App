package com.xvantage.rental.ui.takeRent.bmsheet

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityReceivePaymentBinding
import com.xvantage.rental.network.request.tenant.TenantPaymentRequest
import com.xvantage.rental.network.response.PaymentSummary
import com.xvantage.rental.network.utils.ResultWrapper
import com.xvantage.rental.ui.takeRent.activity.ReceivePaymentViewModel
import com.xvantage.rental.utils.CommonFunction
import com.xvantage.rental.utils.RentalNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ReceivePaymentBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: ActivityReceivePaymentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReceivePaymentViewModel by viewModels()

    private lateinit var rentAmount: String
    private lateinit var rentRcvDate: String
    private lateinit var note: String
    private lateinit var paymentMode: String
    private var electricityMeterReading: String? = null
    private var waterMeterReading: String? = null

    private lateinit var tenantId: String
    private var tenantName: String = ""
    private var roomId: String = ""
    private var propertyName: String = ""
    private var electricityMode: String = ""
    private var waterMode: String = ""
    private var lastElectricityReading: Double = 0.0
    private var lastWaterReading: Double = 0.0
    private var electricityCostPerUnit: Double = 0.0
    private var waterCostPerUnit: Double = 0.0
    private var currentElectricityCharge: Double = 0.0
    private var currentWaterCharge: Double = 0.0


    private var onPaymentReceivedListener: (() -> Unit)? = null

    fun setOnPaymentReceivedListener(listener: () -> Unit) {
        onPaymentReceivedListener = listener
    }

    companion object {

        private const val ARG_TENANT_ID = "tenantId"
        private const val ARG_TENANT_NAME = "tenantName"
        private const val ARG_ROOM_ID = "roomId"
        private const val ARG_PROPERTY_NAME = "propertyName"
        private const val ARG_TOTAL_PAYABLE = "totalPayable"
        private const val ARG_ELECTRICITY_MODE = "electricityMode"
        private const val ARG_WATER_MODE = "waterMode"
        private const val ARG_LAST_METER_READING = "lastMeterReading"
        private const val ARG_LAST_WATER_READING = "lastWaterReading"
        private const val ARG_COST_PER_UNIT = "costPerUnit"
        private const val ARG_COST_UNIT_WATER = "costUnitWater"

        fun newInstance(
            tenantId: String,
            tenantName: String,
            roomId: String,
            propertyName: String,
            totalPayable: Double,
            electricityMode: String,
            waterMode: String,
            lastMeterReading: String,
            lastWaterReading: String,
            costPerUnit: String,
            costUnitWater: String
        ): ReceivePaymentBottomSheetFragment {

            val fragment = ReceivePaymentBottomSheetFragment()

            fragment.arguments = Bundle().apply {
                putString(ARG_TENANT_ID, tenantId)
                putString(ARG_TENANT_NAME, tenantName)
                putString(ARG_ROOM_ID, roomId)
                putString(ARG_PROPERTY_NAME, propertyName)
                putDouble(ARG_TOTAL_PAYABLE, totalPayable)
                putString(ARG_ELECTRICITY_MODE, electricityMode)
                putString(ARG_WATER_MODE, waterMode)
                putString(ARG_LAST_METER_READING, lastMeterReading)
                putString(ARG_LAST_WATER_READING, lastWaterReading)
                putString(ARG_COST_PER_UNIT, costPerUnit)
                putString(ARG_COST_UNIT_WATER, costUnitWater)
            }

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityReceivePaymentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheetDialog = dialog as BottomSheetDialog
            val sheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            sheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.tvTitle.setText(R.string.receive_payment)

        tenantId = arguments?.getString(ARG_TENANT_ID) ?: ""
        tenantName = arguments?.getString(ARG_TENANT_NAME) ?: ""
        roomId = arguments?.getString(ARG_ROOM_ID) ?: ""
        propertyName = arguments?.getString(ARG_PROPERTY_NAME) ?: ""

        val totalPayable = arguments?.getDouble(ARG_TOTAL_PAYABLE) ?: 0.0
        binding.etRentAmount.setText(totalPayable.toInt().toString())

        electricityMode = arguments?.getString(ARG_ELECTRICITY_MODE) ?: ""
        waterMode = arguments?.getString(ARG_WATER_MODE) ?: ""
        lastElectricityReading =
            arguments?.getString(ARG_LAST_METER_READING)?.toDoubleOrNull() ?: 0.0
        lastWaterReading =
            arguments?.getString(ARG_LAST_WATER_READING)?.toDoubleOrNull() ?: 0.0
        electricityCostPerUnit =
            arguments?.getString(ARG_COST_PER_UNIT)?.toDoubleOrNull() ?: 0.0
        waterCostPerUnit =
            arguments?.getString(ARG_COST_UNIT_WATER)?.toDoubleOrNull() ?: 0.0

        setupMeterReadingSections()
        setupTotalSummaryListeners()
        updateTotalPayable()

        val today = java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        binding.etRentRcvDate.setText(today)
        binding.rbCash.isChecked = true

        observePayment()
        setupListeners()
    }

    private fun setupMeterReadingSections() {

        if (electricityMode.equals("metered", ignoreCase = true)) {
            binding.llElectricityMeterSection.visibility = View.VISIBLE
            binding.tvLastElectricityReading.text =
                getString(R.string.last_reading_format, lastElectricityReading.toString())

            binding.etElectricityMeterReading.addTextChangedListener(
                object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: android.text.Editable?) {
                        updateElectricityCharge()
                    }
                }
            )
        } else {
            binding.llElectricityMeterSection.visibility = View.GONE
        }

        if (waterMode.equals("metered", ignoreCase = true)) {
            binding.llWaterMeterSection.visibility = View.VISIBLE
            binding.tvLastWaterReading.text =
                getString(R.string.last_reading_format, lastWaterReading.toString())

            binding.etWaterMeterReading.addTextChangedListener(
                object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: android.text.Editable?) {
                        updateWaterCharge()
                    }
                }
            )
        } else {
            binding.llWaterMeterSection.visibility = View.GONE
        }
    }

    private fun updateElectricityCharge() {
        val current =
            binding.etElectricityMeterReading.text.toString().toDoubleOrNull() ?: 0.0
        val units = (current - lastElectricityReading).coerceAtLeast(0.0)
        val charge = units * electricityCostPerUnit
        currentElectricityCharge = charge
        binding.tvElectricityCalculatedCharge.text =
            getString(R.string.charge_units_format, charge.toLong(), units.toLong())
        updateTotalPayable()
    }

    private fun updateWaterCharge() {
        val current =
            binding.etWaterMeterReading.text.toString().toDoubleOrNull() ?: 0.0
        val units = (current - lastWaterReading).coerceAtLeast(0.0)
        val charge = units * waterCostPerUnit
        currentWaterCharge = charge
        binding.tvWaterCalculatedCharge.text =
            getString(R.string.charge_units_format, charge.toLong(), units.toLong())
        updateTotalPayable()
    }

    /** Rent amount field should also refresh the total live as the owner edits it. */
    private fun setupTotalSummaryListeners() {
        binding.llSummaryElectricityRow.visibility =
            if (electricityMode.equals("metered", ignoreCase = true)) View.VISIBLE else View.GONE
        binding.llSummaryWaterRow.visibility =
            if (waterMode.equals("metered", ignoreCase = true)) View.VISIBLE else View.GONE

        binding.etRentAmount.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    updateTotalPayable()
                }
            }
        )
    }

    /** Recomputes Rent + Electricity + Water and refreshes the Total Payable summary card. */
    private fun updateTotalPayable() {
        val rent = binding.etRentAmount.text.toString().toDoubleOrNull() ?: 0.0

        binding.tvSummaryRent.text = "₹${rent.toLong()}"
        binding.tvSummaryElectricity.text = "₹${currentElectricityCharge.toLong()}"
        binding.tvSummaryWater.text = "₹${currentWaterCharge.toLong()}"

        val total = rent + currentElectricityCharge + currentWaterCharge
        binding.tvTotalPayable.text = "₹${total.toLong()}"


        binding.etAmountReceived.setText(total.toLong().toString())
    }

    private fun setupListeners() {

        binding.toolbar.back.setOnClickListener {
            dismiss()
        }
        binding.toolbar.btnSave.setOnClickListener {
            binding.btnRcvPayment.performClick()
        }

        binding.btnRcvPayment.setOnClickListener {

            if (validateFields()) {

                storeValues()

                val request = TenantPaymentRequest(
                    tenantId = tenantId,
                    roomId = roomId,
                    amount = rentAmount,
                    rent_receive_date = rentRcvDate,
                    payment_mode = paymentMode,
                    note = note,
                    meter_reading = electricityMeterReading,
                    cost_per_unit = if (electricityMeterReading != null) electricityCostPerUnit.toString() else null,
                    meter_reading_water = waterMeterReading,
                    cost_unit_water = if (waterMeterReading != null) waterCostPerUnit.toString() else null
                )

                binding.btnRcvPayment.isEnabled = false
                binding.toolbar.btnSave.isEnabled = false

                viewModel.receivePayment(request)
            }
        }

        binding.etRentRcvDate.setOnClickListener {
            CommonFunction().showDatePickerDialog(
                context = requireContext(),
                onDateSelected = { selectedDate ->
                    binding.etRentRcvDate.setText(selectedDate)
                }
            )
        }
    }

    private fun validateFields(): Boolean {
        val basicValid =
            validateEditText(binding.etRentAmount, getString(R.string.rent_amount_required)) &&
                    validateEditText(binding.etRentRcvDate, getString(R.string.rent_receive_date_required)) &&
                    validateEditText(binding.etAmountReceived, "Please enter amount received")

        if (!basicValid) return false


        if (electricityMode.equals("metered", ignoreCase = true)) {
            val reading = binding.etElectricityMeterReading.text.toString().trim()
            if (reading.isNotEmpty()) {
                val readingValue = reading.toDoubleOrNull()
                if (readingValue == null || readingValue < lastElectricityReading) {
                    binding.etElectricityMeterReading.error =
                        getString(R.string.meter_reading_invalid)
                    binding.etElectricityMeterReading.requestFocus()
                    return false
                }
            }
        }

        if (waterMode.equals("metered", ignoreCase = true)) {
            val reading = binding.etWaterMeterReading.text.toString().trim()
            if (reading.isNotEmpty()) {
                val readingValue = reading.toDoubleOrNull()
                if (readingValue == null || readingValue < lastWaterReading) {
                    binding.etWaterMeterReading.error =
                        getString(R.string.meter_reading_invalid)
                    binding.etWaterMeterReading.requestFocus()
                    return false
                }
            }
        }

        return true
    }

    private fun validateEditText(editText: EditText, message: String): Boolean {
        return if (editText.text.toString().trim().isEmpty()) {
            editText.error = message
            editText.requestFocus()
            false
        } else {
            true
        }
    }

    private fun storeValues() {
        rentAmount = binding.etAmountReceived.text.toString().trim()
            .ifEmpty { binding.etRentAmount.text.toString().trim() }
        rentRcvDate = binding.etRentRcvDate.text.toString().trim()
        paymentMode = getSelectedPaymentMode()
        note = binding.etNote.text.toString().trim()

        electricityMeterReading =
            if (electricityMode.equals("metered", ignoreCase = true))
                binding.etElectricityMeterReading.text.toString().trim()
            else null

        waterMeterReading =
            if (waterMode.equals("metered", ignoreCase = true))
                binding.etWaterMeterReading.text.toString().trim()
            else null

        Log.d("ReceivePayment", "TenantId : $tenantId")
        Log.d("ReceivePayment", "Property : $propertyName")
        Log.d("ReceivePayment", "Room : $roomId")
    }

    private fun observePayment() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.paymentResult.collect { result ->

                when (result) {

                    is ResultWrapper.Success -> {

                        binding.btnRcvPayment.isEnabled = true
                        binding.toolbar.btnSave.isEnabled = true

                        val summary = result.value.data?.paymentSummary
                        if (summary != null) {
                            showPaymentSuccessDialog(summary)
                        } else {
                            showFallbackSuccessDialog()
                        }
                    }

                    is ResultWrapper.Error -> {

                        binding.btnRcvPayment.isEnabled = true
                        binding.toolbar.btnSave.isEnabled = true

//                        Toast.makeText(
//                            requireContext(),
//                            result.message,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun showFallbackSuccessDialog() {

        showPaymentSuccessBottomSheet(
            amountReceived = rentAmount.toDoubleOrNull() ?: 0.0,
            previousDue = 0.0,
            remainingDue = 0.0,
            isFullyPaid = true
        )
    }

    private fun showPaymentSuccessDialog(payment: PaymentSummary) {
        showPaymentSuccessBottomSheet(
            amountReceived = payment.amountReceived,
            previousDue = payment.previousDue,
            remainingDue = payment.remainingDue,
            isFullyPaid = payment.isFullyPaid
        )
    }


    private fun showPaymentSuccessBottomSheet(
        amountReceived: Double,
        previousDue: Double,
        remainingDue: Double,
        isFullyPaid: Boolean
    ) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_payment_success, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)

        val tvAmount = view.findViewById<TextView>(R.id.tvAmountReceived)
        val tvPrevious = view.findViewById<TextView>(R.id.tvPreviousDue)
        val tvRemaining = view.findViewById<TextView>(R.id.tvRemainingDue)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)
        val btnDone = view.findViewById<MaterialButton>(R.id.btnDone)

        tvAmount.text = formatAmount(amountReceived)
        tvPrevious.text = formatAmount(previousDue)
        tvRemaining.text = formatAmount(remainingDue)

        if (isFullyPaid) {
            tvStatus.text = "Tenant dues cleared.\nNo pending amount remaining."
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
        } else {
            tvStatus.text =
                "${formatAmount(remainingDue)} is still pending.\nPlease collect the remaining amount later."
            tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        }

        btnDone.setOnClickListener {
            RentalNotificationHelper.showPaymentReceived(
                context = requireContext(),
                tenantName = tenantName,
                amount = rentAmount
            )
            dialog.dismiss()
            onPaymentReceivedListener?.invoke()
            dismiss()
        }

        dialog.show()


        dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)?.let {
            BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun formatAmount(amount: Double): String {
        return "₹%,.0f".format(amount)
    }

    private fun getSelectedPaymentMode(): String {
        return when (binding.rgPaymentMode.checkedRadioButtonId) {
            R.id.rb_cash -> getString(R.string.cash)
            R.id.rb_upi -> getString(R.string.upi)
            R.id.rb_net_banking -> getString(R.string.net_banking)
            else -> ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}