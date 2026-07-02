package com.xvantage.rental.ui.addProperty.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityPropertyDetailsBinding
import com.xvantage.rental.network.response.PropertyDetailsResponse
import com.xvantage.rental.ui.addProperty.PropertyDetailsViewModel
import com.xvantage.rental.ui.addProperty.bmsheet.AddRoomBottomSheetFragment
import com.xvantage.rental.ui.addProperty.bmsheet.AddTenantBottomSheetFragment
import com.xvantage.rental.ui.addProperty.fragment.FinancialsFragment
import com.xvantage.rental.ui.addProperty.fragment.RoomsFragment
import com.xvantage.rental.ui.addProperty.fragment.TenantsFragment
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PropertyDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPropertyDetailsBinding
    private var propertyId: String = ""
    private lateinit var tabLayoutMediator: TabLayoutMediator
    private val viewModel by viewModels<PropertyDetailsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropertyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.lifecycleOwner = this

        // Get property data from intent
        propertyId =
            intent.getStringExtra("propertyId") ?: ""

        android.util.Log.e(
            "OPEN_PROPERTY_ID",
            propertyId
        )

        setupToolbar()
        setupPropertyDetails()
        setupViewPager()
        setupFab()
    }

    private fun setupToolbar() {
        val backButton = binding.toolbarContainer.findViewById<View>(R.id.back)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupPropertyDetails() {
        viewModel.loadPropertyDetails(propertyId)

        // observe the state & update UI
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is PropertyDetailsViewModel.State.Loading -> {
//                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is PropertyDetailsViewModel.State.Success -> {

                            android.util.Log.e(
                                "PROPERTY_DETAILS",
                                Gson().toJson(state.details.data)
                            )

                            android.util.Log.e(
                                "PROPERTY_ROOMS",
                                Gson().toJson(
                                    state.details.data?.rooms
                                )
                            )

                            bindHeader(state.details)

                            state.details.data?.let {

                                supportFragmentManager.setFragmentResult(
                                    "property_details",
                                    Bundle().apply {

                                        putString(
                                            "property_json",
                                            Gson().toJson(it)
                                        )
                                    }
                                )
                            }
                        }
                        is PropertyDetailsViewModel.State.Error -> {
//                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(this@PropertyDetailsActivity, state.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
    private fun bindHeader(details: PropertyDetailsResponse) {

        binding.tvPropertyName.text =
            details.data?.name ?: ""

        binding.tvPropertyAddress.text =
            details.data?.address ?: ""

        Glide.with(this)
            .load(details.data?.propertyImage)
            .placeholder(R.drawable.image)
            .error(R.drawable.image)
            .into(binding.ivPropertyImage)
    }

    private fun setupViewPager() {
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewPager

        // Create adapter for ViewPager
        val pagerAdapter = PropertyPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Connect TabLayout with ViewPager
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Rooms"
                1 -> "Tenants"
                2 -> "Financials"
                else -> "Tab"
            }
        }
        tabLayoutMediator.attach()

        // Set tab selection listener for showing/hiding FAB based on the selected tab
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateFabVisibility(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            // Show bottom sheet based on current tab
            when (binding.viewPager.currentItem) {
                0 -> showAddRoomBottomSheet()
                1 -> showAddTenantBottomSheet()
                else -> {} // No action for Financials tab
            }
        }

        // Set initial visibility based on default tab
        updateFabVisibility(binding.viewPager.currentItem)
    }

    private fun updateFabVisibility(tabPosition: Int) {
        // Show FAB for Rooms and Tenants tabs, hide for Financials
        binding.fabAdd.visibility = when (tabPosition) {
            0, 1 -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun showAddRoomBottomSheet() {

        val bottomSheet = AddRoomBottomSheetFragment()

        val currentData =
            (viewModel.state.value as? PropertyDetailsViewModel.State.Success)
                ?.details
                ?.data

        val bundle = Bundle()

        bundle.putString(
            "property_json",
            Gson().toJson(currentData)
        )

        bottomSheet.arguments = bundle

        bottomSheet.show(
            supportFragmentManager,
            "AddRoomBottomSheet"
        )
    }

    private fun showAddTenantBottomSheet() {

        val bottomSheet =
            AddTenantBottomSheetFragment()

        val currentData =

            (viewModel.state.value
                    as? PropertyDetailsViewModel.State.Success)
                ?.details
                ?.data

        val bundle = Bundle()

        bundle.putString(
            "property_json",
            Gson().toJson(currentData)
        )

        bottomSheet.arguments =
            bundle

        bottomSheet.show(
            supportFragmentManager,
            "AddTenantBottomSheet"
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detach TabLayoutMediator to prevent memory leaks
        if (::tabLayoutMediator.isInitialized) {
            tabLayoutMediator.detach()
        }
    }

    /**
     * ViewPager adapter for property tabs
     */
    private inner class PropertyPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> RoomsFragment.newInstance(propertyId)
                1 -> TenantsFragment.newInstance(propertyId)
                2 -> FinancialsFragment.newInstance(propertyId)
                else -> Fragment()
            }
        }
    }
}