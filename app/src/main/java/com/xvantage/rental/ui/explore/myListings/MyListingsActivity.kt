package com.xvantage.rental.ui.explore.myListings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.xvantage.rental.databinding.ActivityMyListingsBinding
import com.xvantage.rental.network.response.explore.ExploreListingResponse
import com.xvantage.rental.ui.explore.createListing.CreateListingActivity
import com.xvantage.rental.ui.explore.details.ListingDetailsActivity
import com.xvantage.rental.ui.explore.myListings.adapter.MyListingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyListingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyListingsBinding
    private val viewModel: MyListingsViewModel by viewModels()
    private lateinit var myListingAdapter: MyListingAdapter

    private val statusByTabIndex = listOf(null, "draft", "pending_approval", "approved", "rejected")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyListingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTabs()
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
        observeViewModel()

        viewModel.loadMyListings(statusFilter = null, reset = true)
    }

    override fun onResume() {
        super.onResume()
        // a listing may have just been created/edited/deleted - refresh silently
        viewModel.loadMyListings(reset = true)
    }

    private fun setupToolbar() {
        binding.btnBackMyListings.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupTabs() {
        binding.tabLayoutStatus.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val status = statusByTabIndex.getOrNull(tab.position)
                viewModel.loadMyListings(statusFilter = status, reset = true)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupRecyclerView() {
        myListingAdapter = MyListingAdapter(
            context = this,
            onItemClick = { listing ->
                if (listing.status == "approved") {
                    ListingDetailsActivity.start(this, listing.id)
                } else {
                    CreateListingActivity.startForEdit(this, listing.id)
                }
            },
            onMenuClick = { listing, anchorView -> showItemMenu(listing, anchorView) }
        )
        binding.rvMyListings.apply {
            layoutManager = LinearLayoutManager(this@MyListingsActivity)
            adapter = myListingAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshMyListings.setOnRefreshListener {
            viewModel.loadMyListings(reset = true)
        }
    }

    private fun setupFab() {
        binding.fabAddListing.setOnClickListener {
            CreateListingActivity.startForCreate(this)
        }
        binding.btnStartListingEmptyState.setOnClickListener {
            CreateListingActivity.startForCreate(this)
        }
    }

    private fun showItemMenu(listing: ExploreListingResponse, anchorView: View) {
        val popup = PopupMenu(this, anchorView)
        popup.menu.add("Edit")
        popup.menu.add("Delete")
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title) {
                "Edit" -> {
                    CreateListingActivity.startForEdit(this, listing.id)
                    true
                }
                "Delete" -> {
                    confirmDelete(listing)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun confirmDelete(listing: ExploreListingResponse) {
        AlertDialog.Builder(this)
            .setTitle("Delete listing?")
            .setMessage("\"${listing.title}\" will be permanently removed from Explore. This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteListing(listing.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.listings.collect { listings ->
                        myListingAdapter.setItems(listings)
                        binding.emptyMyListingsLayout.visibility = if (listings.isEmpty()) View.VISIBLE else View.GONE
                        binding.rvMyListings.visibility = if (listings.isEmpty()) View.GONE else View.VISIBLE
                        binding.fabAddListing.visibility = if (listings.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
                launch {
                    viewModel.isLoading.collect { loading ->
                        binding.progressBarMyListings.visibility = if (loading) View.VISIBLE else View.GONE
                        binding.swipeRefreshMyListings.isRefreshing = false
                    }
                }
                launch {
                    viewModel.deleteSuccess.collect { success ->
                        if (success) Toast.makeText(this@MyListingsActivity, "Listing deleted", Toast.LENGTH_SHORT).show()
                    }
                }
                launch {
                    viewModel.errorMessage.collect { message ->
                        message?.let { Toast.makeText(this@MyListingsActivity, it, Toast.LENGTH_SHORT).show() }
                    }
                }
            }
        }
    }
} 