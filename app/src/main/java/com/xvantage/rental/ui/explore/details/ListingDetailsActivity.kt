package com.xvantage.rental.ui.explore.details

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityListingDetailsBinding
import com.xvantage.rental.network.response.explore.ExploreListingResponse
import com.xvantage.rental.ui.explore.common.ExploreConstants
import com.xvantage.rental.ui.explore.common.ExploreUiMapper
import com.xvantage.rental.ui.explore.details.adapter.ListingImagePagerAdapter
import com.xvantage.rental.ui.explore.details.adapter.SharingPriceDisplayAdapter
import com.xvantage.rental.ui.explore.details.adapter.SimilarListingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ListingDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListingDetailsBinding
    private val viewModel by viewModels<ListingDetailsViewModel>()

    private lateinit var similarAdapter: SimilarListingAdapter
    private var listingId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListingDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listingId = intent.getStringExtra(EXTRA_LISTING_ID) ?: ""
        if (listingId.isEmpty()) {
            finish()
            return
        }

        setupToolbarActions()
        setupSimilarListingsRecyclerView()
        observeViewModel()

        viewModel.loadListingDetails(listingId)
    }

    private fun setupToolbarActions() {
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.btnFavoriteDetail.setOnClickListener { viewModel.toggleFavorite() }

        binding.btnCallNow.setOnClickListener { viewModel.recordLeadAndGetContact("call") }
        binding.btnWhatsapp.setOnClickListener { viewModel.recordLeadAndGetContact("whatsapp") }

        binding.tvReportListing.setOnClickListener { showReportDialog() }
    }

    private fun setupSimilarListingsRecyclerView() {
        similarAdapter = SimilarListingAdapter(this) { listing ->
            // opens a fresh details screen for the tapped similar listing
            start(this, listing.id)
        }
        binding.rvSimilarListings.apply {
            layoutManager = LinearLayoutManager(this@ListingDetailsActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = similarAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.listing.collect { listing ->
                        listing?.let { renderListing(it) }
                    }
                }
                launch {
                    viewModel.isLoading.collect { loading ->
                        binding.progressBarDetail.visibility = if (loading) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.isFavorited.collect { favorited ->
                        binding.btnFavoriteDetail.setImageResource(
                            if (favorited) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
                        )
                    }
                }
                launch {
                    viewModel.similarListings.collect { similar ->
                        similarAdapter.setItems(similar)
                        val hasSimilar = similar.isNotEmpty()
                        binding.rvSimilarListings.visibility = if (hasSimilar) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.leadReadyToContact.collect { result ->
                        result?.let { (leadType, contact) -> launchContactIntent(leadType, contact.contactNumber, contact.whatsappNumber) }
                    }
                }
                launch {
                    viewModel.reportSubmitted.collect { submitted ->
                        if (submitted) Toast.makeText(this@ListingDetailsActivity, "Report submitted. Thank you!", Toast.LENGTH_SHORT).show()
                    }
                }
                launch {
                    viewModel.errorMessage.collect { message ->
                        message?.let { Toast.makeText(this@ListingDetailsActivity, it, Toast.LENGTH_SHORT).show() }
                    }
                }
            }
        }
    }

    private fun renderListing(listing: ExploreListingResponse) {
        // ---- gallery ----
        val imageUrls = listing.images
            ?.sortedBy { it.sortOrder }
            ?.mapNotNull { ExploreConstants.listingImageUrl(it.image) }
            ?: emptyList()

        binding.imagePager.adapter = ListingImagePagerAdapter(this, imageUrls)
        TabLayoutMediator(binding.dotsIndicator, binding.imagePager) { _, _ -> }.attach()

        // ---- basic info ----
        binding.tvTitle.text = listing.title
        binding.tvAddress.text = "${listing.locality}, ${listing.city}"
        binding.imgVerifiedDetail.visibility = if (listing.isVerified) View.VISIBLE else View.GONE
        binding.tvPriceRangeDetail.text = ExploreUiMapper.formatPriceRange(listing.priceRange)
        binding.chipGenderDetail.text = ExploreUiMapper.genderLabel(listing.genderPreference)
        binding.chipOccupancyDetail.text = ExploreConstants.occupancyLabels[listing.occupancyFor] ?: listing.occupancyFor

        // ---- sharing prices ----
        binding.rvSharingPrices.apply {
            layoutManager = LinearLayoutManager(this@ListingDetailsActivity)
            adapter = SharingPriceDisplayAdapter(listing.sharingPrices ?: emptyList())
        }

        // ---- food ----
        binding.tvFoodDetail.text = ExploreUiMapper.foodLabel(listing)

        // ---- amenities (rows of 3) ----
        renderAmenities(listing)

        // ---- rules ----
        binding.tvCurfewDetail.text = "Curfew: ${listing.curfewTime ?: "No strict curfew"}"
        binding.tvGuestPolicyDetail.text = listing.guestPolicy?.let { "Guest Policy: $it" } ?: "Guest Policy: Not specified"

        // ---- documents ----
        val documents = listing.documentsRequired
        if (!documents.isNullOrEmpty()) {
            binding.tvDocumentsLabel.visibility = View.VISIBLE
            binding.tvDocumentsDetail.visibility = View.VISIBLE
            binding.tvDocumentsDetail.text = documents.joinToString(", ")
        } else {
            binding.tvDocumentsLabel.visibility = View.GONE
            binding.tvDocumentsDetail.visibility = View.GONE
        }

        // ---- description ----
        binding.tvDescription.text = listing.description ?: "No description provided."
    }

    private fun renderAmenities(listing: ExploreListingResponse) {
        val container = binding.layoutAmenitiesDetail
        container.removeAllViews()

        val amenities = ExploreUiMapper.activeAmenityIcons(listing)
        if (amenities.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No amenities listed"
                setTextColor(0xFF9E9E9E.toInt())
                textSize = 13f
            }
            container.addView(emptyText)
            return
        }

        amenities.chunked(3).forEach { rowItems ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (12 * resources.displayMetrics.density).toInt() }
            }

            rowItems.forEach { (iconRes, label) ->
                val itemLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    gravity = android.view.Gravity.CENTER_HORIZONTAL
                }
                val icon = android.widget.ImageView(this).apply {
                    val size = (28 * resources.displayMetrics.density).toInt()
                    layoutParams = LinearLayout.LayoutParams(size, size)
                    setImageResource(iconRes)
                    imageTintList = android.content.res.ColorStateList.valueOf(0xFF1A1A2E.toInt())
                }
                val labelView = TextView(this).apply {
                    text = label
                    textSize = 11f
                    setTextColor(0xFF555555.toInt())
                    gravity = android.view.Gravity.CENTER_HORIZONTAL
                    setPadding(0, (4 * resources.displayMetrics.density).toInt(), 0, 0)
                }
                itemLayout.addView(icon)
                itemLayout.addView(labelView)
                row.addView(itemLayout)
            }

            container.addView(row)
        }
    }

    private fun launchContactIntent(leadType: String, contactNumber: String, whatsappNumber: String?) {
        try {
            when (leadType) {
                "whatsapp" -> {
                    val number = whatsappNumber ?: contactNumber
                    val url = "https://wa.me/91$number"
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
                else -> {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$contactNumber")))
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open ${if (leadType == "whatsapp") "WhatsApp" else "dialer"}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showReportDialog() {
        val radioGroup = android.widget.RadioGroup(this).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, 0)
        }

        ExploreConstants.reportReasons.forEachIndexed { index, (_, label) ->
            val radioButton = android.widget.RadioButton(this).apply {
                text = label
                id = index
            }
            radioGroup.addView(radioButton)
        }
        radioGroup.check(0)

        val descriptionInput = EditText(this).apply {
            hint = "Additional details (optional)"
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
        }

        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(radioGroup)
            addView(descriptionInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Report this listing")
            .setView(dialogLayout)
            .setPositiveButton("Submit") { _, _ ->
                val selectedIndex = radioGroup.checkedRadioButtonId.coerceAtLeast(0)
                val reasonKey = ExploreConstants.reportReasons[selectedIndex].first
                viewModel.submitReport(reasonKey, descriptionInput.text?.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        private const val EXTRA_LISTING_ID = "explore_listing_id"

        fun start(context: Context, listingId: String) {
            val intent = Intent(context, ListingDetailsActivity::class.java)
            intent.putExtra(EXTRA_LISTING_ID, listingId)
            context.startActivity(intent)
        }
    }
}