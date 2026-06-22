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
    private lateinit var fromDate: String
    private lateinit var toDate: String
    private lateinit var note: String
    private lateinit var paymentMode: String

    private lateinit var tenantId: String

    private var monthlyRent: Double = 0.0
    private var tenantName: String = ""
    private var roomId: String = ""
    private var propertyName: String = ""

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


        val today = java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        layoutBinding.etRentRcvDate.setText(today)

        layoutBinding.etFromDate.setText(today)
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.MONTH, 1)

        val nextMonth = java.text.SimpleDateFormat(
            "yyyy-MM-dd",
            java.util.Locale.getDefault()
        ).format(calendar.time)

        layoutBinding.etToDate.setText(nextMonth)


        layoutBinding.rbCash.isChecked = true

        observePayment()

        setupListeners()
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

                    rent_start_date = fromDate,

                    rent_end_date = toDate,

                    payment_mode = paymentMode,

                    note = note

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
        layoutBinding.etFromDate.setOnClickListener {
            CommonFunction().showDatePickerDialog(
                context = this,
                onDateSelected = { selectedDate ->
                    layoutBinding.etFromDate.setText(selectedDate)
                }
            )
        }
        layoutBinding.etToDate.setOnClickListener {
            CommonFunction().showDatePickerDialog(
                context = this,
                onDateSelected = { selectedDate ->
                    layoutBinding.etToDate.setText(selectedDate)
                }
            )
        }

    }

    private fun validateFields(): Boolean {
        return validateEditText(layoutBinding.etRentAmount, getString(R.string.rent_amount_required)) &&
                validateEditText(layoutBinding.etRentRcvDate, getString(R.string.rent_receive_date_required)) &&
                validateEditText(layoutBinding.etFromDate, getString(R.string.from_date_required)) &&
                validateEditText(layoutBinding.etToDate, getString(R.string.to_date_required))
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
        fromDate = layoutBinding.etFromDate.text.toString().trim()
        toDate = layoutBinding.etToDate.text.toString().trim()
        paymentMode = getSelectedPaymentMode()
        note = layoutBinding.etNote.text.toString().trim()

        Log.d(
            "ReceivePaymentActivity",
            "storeValues: $rentAmount, $rentRcvDate, $fromDate, $toDate, $paymentMode, $note"
        )




        Log.d("ReceivePayment", "TenantId : $tenantId")
        Log.d("ReceivePayment", "Property : $propertyName")
        Log.d("ReceivePayment", "Room : $roomId")
    }
    private fun observePayment() {

        lifecycleScope.launch {

            viewModel.paymentResult.collect { result ->

                when (result) {

                    is ResultWrapper.Success -> {

                        layoutBinding.btnRcvPayment.isEnabled = true
                        layoutBinding.toolbar.btnSave.isEnabled = true

                        Toast.makeText(
                            this@ReceivePaymentActivity,
                            "Payment received successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        RentalNotificationHelper.showPaymentReceived(
                            context = this@ReceivePaymentActivity,
                            tenantName = tenantName,
                            amount = rentAmount
                        )

                        setResult(RESULT_OK)

                        finish()
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


    private fun getSelectedPaymentMode(): String {
        return when (layoutBinding.rgPaymentMode.checkedRadioButtonId) {
            R.id.rb_cash -> getString(R.string.cash)
            R.id.rb_upi -> getString(R.string.upi)
            R.id.rb_net_banking -> getString(R.string.net_banking)
            else -> ""
        }

    }


}
