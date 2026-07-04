package com.xvantage.rental.ui.tenant

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.view.View
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityTenantDetailsBinding
import com.xvantage.rental.ui.addTenant.AddTenantActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.xvantage.rental.ui.dashboard.DashboardActivity

@AndroidEntryPoint
class TenantDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTenantDetailsBinding
    private val viewModel by viewModels<TenantDetailsViewModel>()

    private var tenantId: String = ""
    private var currentStatus = "ACTIVE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = ContextCompat.getColor(this@TenantDetailsActivity, R.color.primary_blue)
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

        tenantId = intent.getStringExtra("tenantId") ?: return
        viewModel.loadTenant(tenantId)

        // Status update result
        lifecycleScope.launch {
            viewModel.statusUpdateState.collect { success ->
                if (success) {
                    val msg = if (currentStatus.equals("ACTIVE", true))
                        "Tenant Deactivated Successfully"
                    else
                        "Tenant Activated Successfully"
                    Toast.makeText(this@TenantDetailsActivity, msg, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    viewModel.loadTenant(tenantId)
                }
            }
        }

        lifecycleScope.launch {

            viewModel.deleteState.collect { success ->

                if (success) {

                    Toast.makeText(
                        this@TenantDetailsActivity,
                        "Tenant deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(
                        this@TenantDetailsActivity,
                        DashboardActivity::class.java
                    )

                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_NEW_TASK

                    startActivity(intent)

                    finish()

                }

            }

        }

        // Tenant data
        lifecycleScope.launch {
            viewModel.tenant.collect { response ->
                response?.let {
                    val data = it.data

                    currentStatus = data.status ?: "ACTIVE"
                    invalidateOptionsMenu()

                    android.util.Log.e("TENANT_DETAILS_STATUS", "ID=$tenantId Status=${data.status}")


                    if (currentStatus.equals("ACTIVE", true)) {
                        binding.tvStatus.text = "ACTIVE TENANT"  // ← NO "●" here
                        binding.statusDot.setBackgroundResource(R.drawable.tenant_dot_pulse)  // green animated dot
                        binding.tvStatus.setTextColor(ContextCompat.getColor(this@TenantDetailsActivity, android.R.color.white))

                        binding.tvStatus.parent.let { parent ->
                            if (parent is android.view.ViewGroup) {
                                parent.setBackgroundResource(R.drawable.tenant_status_badge)
                            }
                        }
                    } else {
                        binding.tvStatus.text = "INACTIVE TENANT"  // ← NO "●" here
                        binding.statusDot.setBackgroundResource(R.drawable.red_status_bg)  // red dot
                        binding.tvStatus.setTextColor(ContextCompat.getColor(this@TenantDetailsActivity, android.R.color.white))
                        // Badge background — red/dark
                        binding.tvStatus.parent.let { parent ->
                            if (parent is android.view.ViewGroup) {
                                parent.setBackgroundResource(R.drawable.red_status_bg)
                            }
                        }
                    }

                    // Basic info
                    binding.tvTenantName.text = data.tenant_name ?: "N/A"
                    binding.tvPhone.text      = data.phone_number ?: "N/A"
                    binding.tvProperty.text   = data.tenant_details?.property?.name ?: "N/A"
                    binding.tvRoom.text       = data.tenant_details?.room_no ?: "N/A"
                    binding.tvRent.text       = "₹${data.rent ?: "0"}"
                    binding.tvDeposit.text    = "₹${data.room_deposit ?: "0"}"
                    binding.tvCheckInDate.text   = data.checkin_date ?: "N/A"
                    binding.tvRentStartDate.text = data.rent_start_date ?: "N/A"


                    when ((data.fixed_electricity ?: "").lowercase().trim()) {
                        "fix", "fixed" -> {
                            binding.tvElectricityPlanBadge.text = "FIXED"
                            binding.tvElectricityPlanBadge.setBackgroundColor(android.graphics.Color.parseColor("#1565C0"))
                            binding.tvElectricity.text = "₹${data.fixed_electricity_amount ?: "0"} / month"
                        }
                        "metered" -> {
                            binding.tvElectricityPlanBadge.text = "METERED"
                            binding.tvElectricityPlanBadge.setBackgroundColor(android.graphics.Color.parseColor("#E65100"))
                            binding.tvElectricity.text =
                                "Current reading: ${data.meter_reading ?: "0"} units"
                        }
                        else -> {
                            binding.tvElectricityPlanBadge.text = "NO COST"
                            binding.tvElectricityPlanBadge.setBackgroundColor(android.graphics.Color.parseColor("#9E9E9E"))
                            binding.tvElectricity.text = "Owner pays"
                        }
                    }

                    // Water - same fix.
                    when ((data.fixed_waterbill ?: "").lowercase().trim()) {
                        "fix", "fixed" -> {
                            binding.tvWaterPlanBadge.text = "FIXED"
                            binding.tvWaterPlanBadge.setBackgroundColor(android.graphics.Color.parseColor("#1565C0"))
                            binding.tvWater.text = "₹${data.fixed_waterbill_amount ?: "0"} / month"
                        }
                        "metered" -> {
                            binding.tvWaterPlanBadge.text = "METERED"
                            binding.tvWaterPlanBadge.setBackgroundColor(android.graphics.Color.parseColor("#E65100"))
                            binding.tvWater.text =
                                "Current reading: ${data.meter_reading_water ?: "0"} units"
                        }
                        else -> {
                            binding.tvWaterPlanBadge.text = "NO COST"
                            binding.tvWaterPlanBadge.setBackgroundColor(android.graphics.Color.parseColor("#9E9E9E"))
                            binding.tvWater.text = "Owner pays"
                        }
                    }


                    val elecIsMetered = (data.fixed_electricity ?: "").lowercase().trim() == "metered"
                    val waterIsMetered = (data.fixed_waterbill ?: "").lowercase().trim() == "metered"

                    binding.llElecUnitCost.visibility =
                        if (elecIsMetered) View.VISIBLE else View.GONE
                    binding.llWaterUnitCost.visibility =
                        if (waterIsMetered) View.VISIBLE else View.GONE
                    binding.llUnitCosts.visibility =
                        if (elecIsMetered || waterIsMetered) View.VISIBLE else View.GONE

                    binding.tvCostPerUnit.text   = "₹${data.cost_per_unit ?: "0"}"
                    binding.tvWaterUnit.text     = "₹${data.cost_unit_water ?: "0"}"

                    // Profile Photo
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tenant_details, menu)
        return true
    }

    // Menu title — current status ke hisab se
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_status)?.title =
            if (currentStatus.equals("ACTIVE", true)) "Deactivate Tenant"
            else "Activate Tenant"
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.action_edit -> {
                val intent = Intent(this, AddTenantActivity::class.java)
                intent.putExtra("tenantId", tenantId)
                startActivity(intent)
                return true
            }

            R.id.action_status -> {
                val isActive = currentStatus.equals("ACTIVE", true)
                val newStatus = if (isActive) "INACTIVE" else "ACTIVE"
                MaterialAlertDialogBuilder(this)
                    .setTitle(if (isActive) "Deactivate Tenant" else "Activate Tenant")
                    .setMessage(
                        if (isActive) "This tenant will be marked as inactive."
                        else "This tenant will be marked as active."
                    )
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.updateTenantStatus(tenantId, newStatus)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                return true
            }

            R.id.action_delete -> {

                MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Tenant")
                    .setMessage("This will permanently delete this tenant and all their payment history, billing records, and documents. This cannot be undone. Are you sure?")
                    .setCancelable(true)
                    .setPositiveButton("Delete") { _, _ ->

                        viewModel.deleteTenantPermanent(tenantId)

                    }
                    .setNegativeButton("Cancel", null)
                    .show()

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}