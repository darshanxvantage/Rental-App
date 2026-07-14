package com.xvantage.rental.ui.rentInvoice

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityRentInvoiceBinding
import com.xvantage.rental.utils.AppPreference

class RentInvoiceActivity : AppCompatActivity() {
    private lateinit var layoutBinding: ActivityRentInvoiceBinding
    lateinit var appPreference: AppPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutBinding = DataBindingUtil.setContentView(this, R.layout.activity_rent_invoice)
        appPreference = AppPreference(this)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        layoutBinding.toolbar.tvTitle.setText(R.string.rent_invoice)
        layoutBinding.toolbar.back.setOnClickListener {
            onBackPressed()
        }

    }
}