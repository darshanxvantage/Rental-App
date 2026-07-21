package com.xvantage.rental.ui.dashboard.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xvantage.rental.R
import com.xvantage.rental.databinding.FragmentDuesBinding
import com.xvantage.rental.network.response.TenantItem
import com.xvantage.rental.ui.takeRent.bmsheet.ReceivePaymentBottomSheetFragment
import com.xvantage.rental.ui.dashboard.fragment.adapter.DuesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DuesFragment : Fragment() {

    private var _binding: FragmentDuesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DuesViewModel by viewModels()
    private lateinit var adapter: DuesAdapter

    // Track which tab is active
    private var currentTab = TAB_ALL

    companion object {
        private const val TAB_ALL     = 0
        private const val TAB_OVERDUE = 1
        private const val TAB_NO_DUE  = 2
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDuesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabListeners()
        observeData()

        viewModel.loadDues()
    }

    // Reload fresh data when user comes back to this fragment
    override fun onResume() {
        super.onResume()

        viewModel.loadDues()

        val listToShow = when (currentTab) {
            TAB_OVERDUE -> viewModel.getOverdueTenants()
            TAB_NO_DUE  -> viewModel.getNoDueTenants()
            else -> viewModel.getAllDueTenants()
        }

        showList(listToShow)
        updateTabUI()
    }

    private fun setupRecyclerView() {
        adapter = DuesAdapter(
            requireContext(),
            viewModel
        ) { tenant ->

            val amountToCollect =
                (tenant.totalDue ?: 0.0).takeIf { it > 0 }
                    ?: (tenant.rent?.toDoubleOrNull() ?: 0.0)

            fun openReceivePaymentSheet() {

                val sheet = ReceivePaymentBottomSheetFragment.newInstance(
                    tenantId = tenant.id,
                    tenantName = tenant.tenant_name ?: "",
                    roomId = tenant.room_fk ?: "",
                    propertyName = tenant.tenant_details?.property?.name ?: "",
                    totalPayable = amountToCollect,
                    electricityMode = tenant.fixed_electricity ?: "",
                    waterMode = tenant.fixed_waterbill ?: "",

                    lastMeterReading = tenant.meter_reading
                        ?.takeIf { it.isNotBlank() }
                        ?: (tenant.last_meter_reading ?: ""),
                    lastWaterReading = tenant.meter_reading_water
                        ?.takeIf { it.isNotBlank() }
                        ?: (tenant.last_meter_reading_water ?: ""),
                    costPerUnit = tenant.cost_per_unit ?: "",
                    costUnitWater = tenant.cost_unit_water ?: ""
                )

                sheet.setOnPaymentReceivedListener {
                    viewModel.loadDues()
                }

                sheet.show(childFragmentManager, "ReceivePayment")
            }

            if ((tenant.totalDue ?: 0.0) <= 0) {
                com.xvantage.rental.ui.common.AdvancePaymentDialog.show(
                    context = requireContext(),
                    tenantName = tenant.tenant_name ?: "",
                    amount = amountToCollect,
                    onConfirm = { openReceivePaymentSheet() }
                )
            } else {
                openReceivePaymentSheet()
            }
        }
        binding.rvDues.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDues.adapter = adapter
    }

    private fun setupTabListeners() {
        binding.tabAll.setOnClickListener {
            currentTab = TAB_ALL
            updateTabUI()
            showList(viewModel.getAllDueTenants())
        }
        binding.tabOverdue.setOnClickListener {
            currentTab = TAB_OVERDUE
            updateTabUI()
            showList(viewModel.getOverdueTenants())
        }
        binding.tabNoDue.setOnClickListener {
            currentTab = TAB_NO_DUE
            updateTabUI()
            showList(viewModel.getNoDueTenants())
        }
    }

    private fun observeData() {
        // Loading state
        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                binding.progressBar.visibility =
                    if (loading) View.VISIBLE else View.GONE
                if (loading) {
                    binding.rvDues.visibility    = View.GONE
                    binding.layoutEmpty.visibility = View.GONE
                }
            }
        }

        // Tenant list
        lifecycleScope.launch {
            viewModel.allTenants.collect { tenants ->
                // Update summary card
                updateSummaryCard(tenants)

                // Show correct list based on active tab
                val listToShow = when (currentTab) {
                    TAB_OVERDUE -> viewModel.getOverdueTenants()
                    TAB_NO_DUE  -> viewModel.getNoDueTenants()
                    else -> viewModel.getAllDueTenants()
                }
                updateTabUI()
                showList(listToShow)
            }
        }
    }

    private fun updateSummaryCard(tenants: List<TenantItem>) {
        val totalDues = tenants.sumOf { it.totalDue ?: 0.0 }
        binding.tvTotalDues.text = "₹${totalDues.toLong()}"

        val overdueCount = tenants.count { it.hasOverdue == true }
        binding.tvOverdueCount.text = overdueCount.toString()

        // Total active tenant count
        binding.tvActiveTenants.text = tenants.size.toString()
    }

    private fun showList(list: List<TenantItem>) {
        if (list.isEmpty()) {
            binding.rvDues.visibility     = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
        } else {
            binding.rvDues.visibility     = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            adapter.submitList(list)
        }
    }

    private fun updateTabUI() {

        val blue = ContextCompat.getColor(requireContext(), R.color.royal_blue)
        val white = ContextCompat.getColor(requireContext(), android.R.color.white)

        // ALL
        if (currentTab == TAB_ALL) {
            binding.tabAll.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabAll.setTextColor(white)
        } else {
            binding.tabAll.setBackgroundResource(R.drawable.bg_tab_unselected)
            binding.tabAll.setTextColor(blue)
        }

        // OVERDUE
        if (currentTab == TAB_OVERDUE) {
            binding.tabOverdue.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabOverdue.setTextColor(white)
        } else {
            binding.tabOverdue.setBackgroundResource(R.drawable.bg_tab_unselected)
            binding.tabOverdue.setTextColor(blue)
        }

        // NO DUE
        if (currentTab == TAB_NO_DUE) {
            binding.tabNoDue.setBackgroundResource(R.drawable.bg_tab_selected)
            binding.tabNoDue.setTextColor(white)
        } else {
            binding.tabNoDue.setBackgroundResource(R.drawable.bg_tab_unselected)
            binding.tabNoDue.setTextColor(blue)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}