package com.xvantage.rental.ui.takeRent.activity

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityReceivePaymentBinding
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.CommonFunction
import android.app.Dialog
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.xvantage.rental.network.response.PaymentSummary
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.xvantage.rental.network.utils.ResultWrapper
import android.widget.Toast
import com.xvantage.rental.network.request.tenant.TenantPaymentRequest
import com.xvantage.rental.utils.RentalNotificationHelper
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ReceivePaymentActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityReceivePaymentBinding
    private lateinit var appPreference: AppPreference
    private val viewModel: ReceivePaymentViewModel by viewModels()
    private lateinit var rentAmount: String
    private lateinit var rentRcvDate: String
    private lateinit var note: String
    private lateinit var paymentMode: String
    private var electricityMeterReading: String? = null
    private var waterMeterReading: String? = null

    private lateinit var tenantId: String

    private var monthlyRent: Double = 0.0
    private var tenantName: String = ""
    private var roomId: String = ""
    private var propertyName: String = ""
    private var electricityMode: String = ""
    private var waterMode: String = ""
    private var lastElectricityReading: Double = 0.0
    private var lastWaterReading: Double = 0.0
    private var electricityCostPerUnit: Double = 0.0
    private var waterCostPerUnit: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutBinding = DataBindingUtil.setContentView(this, R.layout.activity_receive_payment)
        appPreference = AppPreference(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        layoutBinding.toolbar.tvTitle.setText(R.string.receive_payment)
        tenantId = intent.getStringExtra("tenantId") ?: ""

        tenantName = intent.getStringExtra("tenantName") ?: ""

        roomId = intent.getStringExtra("roomId") ?: ""

        propertyName = intent.getStringExtra("propertyName") ?: ""

        val totalPayable =
            intent.getDoubleExtra("totalPayable", 0.0)

        layoutBinding.etRentAmount.setText(
            totalPayable.toInt().toString()
        )

        electricityMode = intent.getStringExtra("electricityMode") ?: ""
        waterMode = intent.getStringExtra("waterMode") ?: ""
        lastElectricityReading =
            intent.getStringExtra("lastMeterReading")?.toDoubleOrNull() ?: 0.0
        lastWaterReading =
            intent.getStringExtra("lastWaterReading")?.toDoubleOrNull() ?: 0.0
        electricityCostPerUnit =
            intent.getStringExtra("costPerUnit")?.toDoubleOrNull() ?: 0.0
        waterCostPerUnit =
            intent.getStringExtra("costUnitWater")?.toDoubleOrNull() ?: 0.0

        setupMeterReadingSections()


        val today = java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        layoutBinding.etRentRcvDate.setText(today)


        layoutBinding.rbCash.isChecked = true

        observePayment()

        setupListeners()
    }
    private fun setupMeterReadingSections() {

        if (electricityMode.equals("metered", ignoreCase = true)) {
            layoutBinding.llElectricityMeterSection.visibility = android.view.View.VISIBLE
            layoutBinding.tvLastElectricityReading.text =
                getString(R.string.last_reading_format, lastElectricityReading.toString())

            layoutBinding.etElectricityMeterReading.addTextChangedListener(
                object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: android.text.Editable?) {
                        updateElectricityCharge()
                    }
                }
            )
        } else {
            layoutBinding.llElectricityMeterSection.visibility = android.view.View.GONE
        }

        if (waterMode.equals("metered", ignoreCase = true)) {
            layoutBinding.llWaterMeterSection.visibility = android.view.View.VISIBLE
            layoutBinding.tvLastWaterReading.text =
                getString(R.string.last_reading_format, lastWaterReading.toString())

            layoutBinding.etWaterMeterReading.addTextChangedListener(
                object : android.text.TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: android.text.Editable?) {
                        updateWaterCharge()
                    }
                }
            )
        } else {
            layoutBinding.llWaterMeterSection.visibility = android.view.View.GONE
        }
    }

    private fun updateElectricityCharge() {
        val current =
            layoutBinding.etElectricityMeterReading.text.toString().toDoubleOrNull() ?: 0.0
        val units = (current - lastElectricityReading).coerceAtLeast(0.0)
        val charge = units * electricityCostPerUnit
        layoutBinding.tvElectricityCalculatedCharge.text =
            getString(R.string.charge_units_format, charge.toLong(), units.toLong())
    }

    private fun updateWaterCharge() {
        val current =
            layoutBinding.etWaterMeterReading.text.toString().toDoubleOrNull() ?: 0.0
        val units = (current - lastWaterReading).coerceAtLeast(0.0)
        val charge = units * waterCostPerUnit
        layoutBinding.tvWaterCalculatedCharge.text =
            getString(R.string.charge_units_format, charge.toLong(), units.toLong())
    }


    private fun setupListeners() {
        layoutBinding.toolbar.back.setOnClickListener {
            finish()
        }
        layoutBinding.toolbar.btnSave.setOnClickListener {
            layoutBinding.btnRcvPayment.performClick()
        }

        layoutBinding.btnRcvPayment.setOnClickListener {

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
                layoutBinding.btnRcvPayment.isEnabled = false
                layoutBinding.toolbar.btnSave.isEnabled = false

                viewModel.receivePayment(request)

            }

        }



        layoutBinding.etRentRcvDate.setOnClickListener {
            CommonFunction().showDatePickerDialog(
                context = this,
                onDateSelected = { selectedDate ->
                    layoutBinding.etRentRcvDate.setText(selectedDate)
                }
            )
        }

    }

    private fun validateFields(): Boolean {
        val basicValid =
            validateEditText(layoutBinding.etRentAmount, getString(R.string.rent_amount_required)) &&
                    validateEditText(layoutBinding.etRentRcvDate, getString(R.string.rent_receive_date_required))

        if (!basicValid) return false

        if (electricityMode.equals("metered", ignoreCase = true)) {
            val reading = layoutBinding.etElectricityMeterReading.text.toString().trim()
            if (reading.isEmpty()) {
                layoutBinding.etElectricityMeterReading.error =
                    getString(R.string.meter_reading_required)
                layoutBinding.etElectricityMeterReading.requestFocus()
                return false
            }
            val readingValue = reading.toDoubleOrNull()
            if (readingValue == null || readingValue < lastElectricityReading) {
                layoutBinding.etElectricityMeterReading.error =
                    getString(R.string.meter_reading_invalid)
                layoutBinding.etElectricityMeterReading.requestFocus()
                return false
            }
        }

        if (waterMode.equals("metered", ignoreCase = true)) {
            val reading = layoutBinding.etWaterMeterReading.text.toString().trim()
            if (reading.isEmpty()) {
                layoutBinding.etWaterMeterReading.error =
                    getString(R.string.meter_reading_required)
                layoutBinding.etWaterMeterReading.requestFocus()
                return false
            }
            val readingValue = reading.toDoubleOrNull()
            if (readingValue == null || readingValue < lastWaterReading) {
                layoutBinding.etWaterMeterReading.error =
                    getString(R.string.meter_reading_invalid)
                layoutBinding.etWaterMeterReading.requestFocus()
                return false
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
        rentAmount = layoutBinding.etRentAmount.text.toString().trim()
        rentRcvDate = layoutBinding.etRentRcvDate.text.toString().trim()
        paymentMode = getSelectedPaymentMode()
        note = layoutBinding.etNote.text.toString().trim()

        electricityMeterReading =
            if (electricityMode.equals("metered", ignoreCase = true))
                layoutBinding.etElectricityMeterReading.text.toString().trim()
            else null

        waterMeterReading =
            if (waterMode.equals("metered", ignoreCase = true))
                layoutBinding.etWaterMeterReading.text.toString().trim()
            else null

        Log.d("ReceivePayment", "TenantId : $tenantId")
        Log.d("ReceivePayment", "Property : $propertyName")
        Log.d("ReceivePayment", "Room : $roomId")
    }
    private fun observePayment() {

        lifecycleScope.launch {

            viewModel.paymentResult.collect { result ->

                when (result) {

                    is ResultWrapper.Success -> {

                        Log.d("PAYMENT_POPUP", "SUCCESS")

                        layoutBinding.btnRcvPayment.isEnabled = true
                        layoutBinding.toolbar.btnSave.isEnabled = true
                    }

                    is ResultWrapper.Error -> {

                        layoutBinding.btnRcvPayment.isEnabled = true

                        layoutBinding.toolbar.btnSave.isEnabled = true

                        Toast.makeText(
                            this@ReceivePaymentActivity,
                            result.message,
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                    else -> {}

                }

            }

        }

    }

    private fun showPaymentSuccessDialog(
        payment: PaymentSummary
    ) {

        Log.d("PAYMENT_POPUP", "DIALOG OPEN")

        val dialog = Dialog(this)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog.setContentView(R.layout.dialog_payment_success)

        dialog.setCancelable(false)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.window?.setLayout(

            WindowManager.LayoutParams.MATCH_PARENT,

            WindowManager.LayoutParams.WRAP_CONTENT

        )

        val tvAmount =
            dialog.findViewById<TextView>(R.id.tvAmountReceived)

        val tvPrevious =
            dialog.findViewById<TextView>(R.id.tvPreviousDue)

        val tvRemaining =
            dialog.findViewById<TextView>(R.id.tvRemainingDue)

        val tvStatus =
            dialog.findViewById<TextView>(R.id.tvStatus)

        val btnDone =
            dialog.findViewById<MaterialButton>(R.id.btnDone)

        tvAmount.text =
            formatAmount(payment.amountReceived)

        tvPrevious.text =
            formatAmount(payment.previousDue)

        tvRemaining.text =
            formatAmount(payment.remainingDue)

        if (payment.isFullyPaid) {

            tvStatus.text =
                "Tenant dues cleared.\nNo pending amount remaining."

            tvStatus.setTextColor(

                getColor(R.color.green)

            )

        } else {

            tvStatus.text =
                "${formatAmount(payment.remainingDue)} is still pending.\nPlease collect the remaining amount later."

            tvStatus.setTextColor(

                getColor(R.color.red)

            )

        }

        btnDone.setOnClickListener {

            RentalNotificationHelper.showPaymentReceived(

                context = this,

                tenantName = tenantName,

                amount = rentAmount

            )

            val resultIntent = intent.apply {

                putExtra("payment_updated", true)

            }

            setResult(

                RESULT_OK,

                resultIntent

            )

            dialog.dismiss()

            finish()

        }

        dialog.show()

    }

    private fun formatAmount(
        amount: Double
    ): String {

        return "₹%,.0f".format(amount)

    }


    private fun getSelectedPaymentMode(): String {
        return when (layoutBinding.rgPaymentMode.checkedRadioButtonId) {
            R.id.rb_cash -> getString(R.string.cash)
            R.id.rb_upi -> getString(R.string.upi)
            R.id.rb_net_banking -> getString(R.string.net_banking)
            else -> ""
        }

    }


}