package com.xvantage.rental.ui.takeRent.activity

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityReceivePaymentBinding
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.CommonFunction
import android.os.Build

class ReceivePaymentActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityReceivePaymentBinding
    private lateinit var appPreference: AppPreference
    private lateinit var rentAmount: String
    private lateinit var rentRcvDate: String
    private lateinit var fromDate: String
    private lateinit var toDate: String
    private lateinit var note: String
    private lateinit var paymentMode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutBinding = DataBindingUtil.setContentView(this, R.layout.activity_receive_payment)
        appPreference = AppPreference(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        layoutBinding.toolbar.tvTitle.setText(R.string.receive_payment)
        setupListeners()
    }


    private fun setupListeners() {
        layoutBinding.toolbar.back.setOnClickListener {
            onBackPressed()
            finish()
        }
        layoutBinding.toolbar.btnSave.setOnClickListener {
            if (validateFields()) {
                storeValues()
                onBackPressed()
                finish()
            }
        }

        layoutBinding.btnRcvPayment.setOnClickListener {
            if (validateFields()) {
                storeValues()
                onBackPressed()
                finish()
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
                validateEditText(layoutBinding.etToDate, getString(R.string.to_date_required)) &&
                validateEditText(layoutBinding.etNote, getString(R.string.note_required))
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

        // ✅ YEH ADD KARO — payment receive hone par notification
        val tenantName = intent.getStringExtra("tenant_name") ?: "Tenant"
        com.xvantage.rental.utils.RentalNotificationHelper.showPaymentReceived(
            context = this,
            tenantName = tenantName,
            amount = rentAmount
        )
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
