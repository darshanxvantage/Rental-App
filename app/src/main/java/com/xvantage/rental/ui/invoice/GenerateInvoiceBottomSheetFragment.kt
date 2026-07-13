package com.xvantage.rental.ui.invoice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.xvantage.rental.data.remote.APIInterface
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xvantage.rental.R
import com.xvantage.rental.data.model.BillingCycle
import kotlinx.coroutines.launch

/**
 * "Generate Invoice for any month" bottom sheet.
 *
 * Usage from the tenant screen:
 *   GenerateInvoiceBottomSheetFragment.newInstance(tenantId)
 *       .show(childFragmentManager, "generate_invoice")
 *
 * Wire ONE thing before using this: replace the `apiService` line in
 * onViewCreated() with however your app actually obtains its Retrofit
 * instance (Hilt/Koin injection, a singleton object, etc.) — that's
 * the only project-specific piece I couldn't see from here.
 */
@AndroidEntryPoint
class GenerateInvoiceBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_TENANT_ID = "tenant_id"

        fun newInstance(tenantId: String): GenerateInvoiceBottomSheetFragment {
            val fragment = GenerateInvoiceBottomSheetFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_TENANT_ID, tenantId)
            }
            return fragment
        }
    }

    private lateinit var tenantId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvError: TextView
    private lateinit var adapter: BillingCycleAdapter

    // TODO: replace with your app's actual DI / singleton pattern.
    @Inject
    lateinit var apiService: APIInterface

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_generate_invoice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tenantId = requireArguments().getString(ARG_TENANT_ID)
            ?: throw IllegalArgumentException("tenantId is required")

        recyclerView = view.findViewById(R.id.recyclerBillingCycles)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        tvError = view.findViewById(R.id.tvError)

        adapter = BillingCycleAdapter { cycle -> onCycleSelected(cycle) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadBillingCycles()
    }

    private fun loadBillingCycles() {
        showLoading()

        lifecycleScope.launch {
            try {
                val response = apiService.getBillingCycles(tenantId)

                if (response.isSuccessful && response.body()?.success == true) {
                    val cycles = response.body()?.data.orEmpty()
                    if (cycles.isEmpty()) {
                        showEmpty()
                    } else {
                        showList(cycles)
                    }
                } else {
                    showError(response.body()?.message ?: "Failed to load billing months")
                }
            } catch (e: Exception) {
                showError(e.message ?: "Something went wrong")
            }
        }
    }

    private fun onCycleSelected(cycle: BillingCycle) {
        showLoading()

        lifecycleScope.launch {
            try {
                val response = apiService.generateInvoiceForMonth(tenantId, cycle.cycleMonth)

                if (response.isSuccessful && response.body()?.success == true) {
                    val filePath = response.body()?.data?.filePath
                    if (filePath != null) {
//                        Toast.makeText(requireContext(), "Invoice generated", Toast.LENGTH_SHORT).show()
                        // TODO: hand off `filePath` to however you already open/download
                        // generated PDFs elsewhere in the app (e.g. same flow used for
                        // the statement / current-month invoice download).
                        onInvoiceGenerated(filePath)
                        dismiss()
                    } else {
                        showList(currentList())
//                        Toast.makeText(requireContext(), "Invoice generated but no file path returned", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val message = response.body()?.message ?: "Failed to generate invoice"
                    showList(currentList())
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                showList(currentList())
//                Toast.makeText(requireContext(), e.message ?: "Something went wrong", Toast.LENGTH_LONG).show()
            }
        }
    }

    /** Override or pass a callback in from the host screen as needed. */
    private fun onInvoiceGenerated(filePath: String) {

        val baseUrl = "https://api.rental.xvantageinfotech.com"

        val pdfUrl = if (filePath.startsWith("http")) {
            filePath
        } else {
            baseUrl + filePath
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(pdfUrl)
            }

            startActivity(intent)

        } catch (e: Exception) {
//            Toast.makeText(
//                requireContext(),
//                "Unable to open invoice PDF",
//                Toast.LENGTH_LONG
//            ).show()
        }
    }

    private var lastLoadedCycles: List<BillingCycle> = emptyList()
    private fun currentList(): List<BillingCycle> = lastLoadedCycles

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        tvError.visibility = View.GONE
    }

    private fun showList(cycles: List<BillingCycle>) {
        lastLoadedCycles = cycles
        adapter.submitList(cycles)
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        tvError.visibility = View.GONE
    }

    private fun showEmpty() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.VISIBLE
        tvError.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        tvEmpty.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        tvError.text = message
    }
}