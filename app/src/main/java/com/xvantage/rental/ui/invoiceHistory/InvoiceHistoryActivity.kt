package com.xvantage.rental.ui.invoiceHistory

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityInvoiceHistoryBinding
import com.xvantage.rental.utils.AppPreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InvoiceHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInvoiceHistoryBinding
    private lateinit var appPreference: AppPreference
    private val viewModel: InvoiceHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoiceHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPreference = AppPreference(this)

        binding.toolbar.tvTitle.setText(R.string.invoice_history)
        binding.toolbar.back.setOnClickListener {
            onBackPressed()
        }

        binding.rvInvoiceList.layoutManager = LinearLayoutManager(this)

        val tenantId = intent.getStringExtra("tenantId")

        observeInvoices()
        viewModel.loadInvoices(tenantId)
    }

    private fun observeInvoices() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                binding.progressBar.visibility =
                    if (loading) View.VISIBLE else View.GONE
                if (loading) {
                    binding.rvInvoiceList.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.invoices.collect { invoices ->
                if (invoices.isEmpty()) {
                    binding.rvInvoiceList.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                } else {
                    binding.rvInvoiceList.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rvInvoiceList.adapter = InvoiceHistoryAdapter(invoices, this@InvoiceHistoryActivity)
                }
            }
        }
    }
}