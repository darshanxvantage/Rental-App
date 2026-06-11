package com.xvantage.rental.ui.dashboard.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.viewModels
import com.xvantage.rental.ui.dashboard.PropertyListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.xvantage.rental.databinding.FragmentHomeBinding
import com.xvantage.rental.ui.addTenant.AddTenantActivity
import com.xvantage.rental.ui.dashboard.DashboardActivity
import com.xvantage.rental.ui.dashboard.fragment.adapter.PropertiesAdapter
import com.xvantage.rental.ui.dashboard.fragment.adapter.TenantsAdapter
import com.xvantage.rental.ui.manageProperty.ManagePropertyActivity
import com.xvantage.rental.ui.takeRent.activity.TakeRentActivity
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.CommonFunction
import kotlinx.coroutines.DelicateCoroutinesApi
import com.xvantage.rental.data.source.sample.PropertyDataRepository
import com.xvantage.rental.ui.dashboard.TenantListViewModel
import com.xvantage.rental.ui.tenant.TenantListActivity
import android.widget.TextView
import com.xvantage.rental.R

@DelicateCoroutinesApi
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var appPreference: AppPreference
    private lateinit var propertiesAdapter: PropertiesAdapter
    private lateinit var tenantsAdapter: TenantsAdapter
    private lateinit var dashboardActivity: DashboardActivity

    private val propertyViewModel: PropertyListViewModel by viewModels()

    private val tenantViewModel: TenantListViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dashboardActivity = context as DashboardActivity
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        setupClickListeners()
        setupRecyclerViews()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        tenantViewModel.loadTenants()
        propertyViewModel.loadProperties()
    }

    private fun initializeViews() {
        appPreference = AppPreference(requireContext())
    }

    private fun setupClickListeners() {


        binding.tvViewAllPropperty.setOnClickListener {
            CommonFunction().navigation(requireContext(), ManagePropertyActivity::class.java)
        }
        binding.cvQuickAction.cvTakeRent.setOnClickListener {
            CommonFunction().navigation(requireContext(), TakeRentActivity::class.java)
        }
        binding.cvQuickAction.cvAddTenant.setOnClickListener {
            CommonFunction().navigation(requireContext(), AddTenantActivity::class.java)
        }
        binding.cvQuickAction.cvAddProperty.setOnClickListener {
            CommonFunction().navigation(requireContext(), ManagePropertyActivity::class.java)
        }
        binding.tvViewAllTenant.setOnClickListener {

            CommonFunction().navigation(
                requireContext(),
                TenantListActivity::class.java
            )
        }

    }

    private fun navigateToTakeRent() {
        CommonFunction().navigation(requireContext(), TakeRentActivity::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupRecyclerViews() {
        setupPropertiesRecyclerView()
        setupTenantsRecyclerView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupPropertiesRecyclerView() {

        propertiesAdapter = PropertiesAdapter(requireContext())

        binding.horizontalRecyclerView1.apply {
            adapter = propertiesAdapter
            layoutManager = createHorizontalLayoutManager()
        }

        propertyViewModel.loadProperties()
        lifecycleScope.launch {

            propertyViewModel.propertyList.collect {

                android.util.Log.e(
                    "PROPERTY_LIST_SIZE",
                    it.size.toString()
                )

                propertiesAdapter.addItems(it)

                val propertyCountTv =
                    requireView().findViewById<TextView>(
                        R.id.tvPropertyCount
                    )

                propertyCountTv.text =
                    it.size.toString()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupTenantsRecyclerView() {

        tenantsAdapter =
            TenantsAdapter(requireContext())

        binding.horizontalRecyclerView2.apply {

            adapter =
                tenantsAdapter

            layoutManager =
                createHorizontalLayoutManager()
        }

        tenantViewModel.loadTenants()

        lifecycleScope.launch {

            tenantViewModel.tenantList.collect {

                tenantsAdapter.addItems(it)

                val tenantCountTv =
                    requireView().findViewById<TextView>(
                        R.id.tvTenantCount
                    )

                tenantCountTv.text =
                    it.size.toString()
            }
        }
    }

    private fun createHorizontalLayoutManager() = LinearLayoutManager(
        requireContext(),
        LinearLayoutManager.HORIZONTAL,
        false
    )

    companion object {
        const val SERVER_KEY = "PushNotificationServerKey"
    }
}