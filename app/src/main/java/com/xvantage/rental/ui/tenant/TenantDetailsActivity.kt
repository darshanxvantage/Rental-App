package com.xvantage.rental.ui.tenant

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityTenantDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TenantDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTenantDetailsBinding
    private val viewModel by viewModels<TenantDetailsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = ContextCompat.getColor(
                this@TenantDetailsActivity,
                R.color.primary_blue
            )
        }

        binding = ActivityTenantDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.navigationIcon?.setTint(
            ContextCompat.getColor(this, android.R.color.black)
        )

        val tenantId = intent.getStringExtra("tenantId") ?: return

        viewModel.loadTenant(tenantId)

        lifecycleScope.launch {
            viewModel.tenant.collect { response ->
                response?.let {
                    val data = it.data


                    binding.tvTenantName.text = data.tenant_name ?: "N/A"
                    binding.tvPhone.text     = data.phone_number ?: "N/A"


                    binding.tvProperty.text = data.tenant_details?.property?.name ?: "N/A"
                    binding.tvRoom.text     = data.tenant_details?.room_no ?: "N/A"


                    binding.tvRent.text    = "₹${data.rent ?: "0"}"
                    binding.tvDeposit.text = "₹${data.room_deposit ?: "0"}"


                    binding.tvCheckInDate.text   = data.checkin_date ?: "N/A"
                    binding.tvRentStartDate.text = data.rent_start_date ?: "N/A"
                    binding.tvElectricity.text   = "₹${data.fixed_electricity_amount ?: "0"}"
                    binding.tvWater.text         = "₹${data.fixed_waterbill_amount ?: "0"}"
                    binding.tvCostPerUnit.text   = "₹${data.cost_per_unit ?: "0"}"
                    binding.tvWaterUnit.text     = "₹${data.cost_unit_water ?: "0"}"
//                    binding.tvNotes.text         = data.note ?: "No notes available."

                    // ── Profile Photo — Circle Round Shape ──
                    Glide.with(this@TenantDetailsActivity)
                        .load(data.profile_pic)
                        .apply(
                            RequestOptions()
                                .transform(CircleCrop())
                                .placeholder(R.drawable.image)
                                .error(R.drawable.image)
                        )
                        .into(binding.ivTenant)
                }
            }
        }
    }
}