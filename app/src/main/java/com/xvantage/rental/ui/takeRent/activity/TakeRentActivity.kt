package com.xvantage.rental.ui.takeRent.activity

import    android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityTakeRentBinding
import com.xvantage.rental.ui.takeRent.adapter.PropertyRoomAdapter
import com.xvantage.rental.utils.AppPreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine

@AndroidEntryPoint
class TakeRentActivity : AppCompatActivity() {

    private lateinit var layoutBinding: ActivityTakeRentBinding
    private val viewModel: TakeRentViewModel by viewModels()
    lateinit var appPreference: AppPreference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutBinding = DataBindingUtil.setContentView(this, R.layout.activity_take_rent)
        appPreference = AppPreference(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        layoutBinding.toolbar.tvTitle.setText(R.string.take_rent)
        layoutBinding.toolbar.back.setOnClickListener { finish() }
        layoutBinding.rvPropertyList.layoutManager = LinearLayoutManager(this)


        viewModel.loadData()

        lifecycleScope.launch {

            combine(
                viewModel.propertyList,
                viewModel.tenantList
            ) { properties, tenants ->

                buildPropertyData(properties, tenants)

            }.collect { propertyData ->

                layoutBinding.rvPropertyList.adapter =
                    PropertyRoomAdapter(
                        propertyData,
                        this@TakeRentActivity
                    ) { tenantId ->
                        viewModel.generateCompleteStatement(tenantId)
                    }

            }

        }

        lifecycleScope.launch {
            viewModel.isGeneratingStatement.collect { generating ->
                if (generating) {
                    android.widget.Toast.makeText(
                        this@TakeRentActivity,
                        "Generating statement…",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.statementFilePath.collect { filePath ->
                if (filePath != null) {
                    openStatementPdf(filePath)
                    viewModel.clearStatementFilePath()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.errorMsg.collect { msg ->
                if (msg != null) {
                    android.widget.Toast.makeText(
                        this@TakeRentActivity,
                        msg,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun openStatementPdf(relativePath: String) {
        try {
            val fullUrl = com.xvantage.rental.utils.constants.Constant.SERVER_ROOT_URL + relativePath
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(android.net.Uri.parse(fullUrl), "application/pdf")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val fullUrl = com.xvantage.rental.utils.constants.Constant.SERVER_ROOT_URL + relativePath
                startActivity(
                    android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(fullUrl))
                )
            } catch (e2: Exception) {
                android.widget.Toast.makeText(
                    this,
                    "Couldn't open the statement. Please try again.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun buildPropertyData(
        properties: List<com.xvantage.rental.network.response.PropertyItem>,
        tenants: List<com.xvantage.rental.network.response.TenantItem>
    ): List<PropertyItem> {
        return properties.map { property ->
            PropertyItem(
                propertyName = property.name,
                propertyId   = property.id,
                rooms = property.property_room_no.map { room ->
                    val tenant = tenants.firstOrNull { t ->
                        t.property_fk == property.id &&
                                t.tenant_details?.room_no == room.room_no &&
                                t.status.equals("ACTIVE", ignoreCase = true)
                    }

                    android.util.Log.d(
                        "TENANT_IMAGE",
                        "Tenant = ${tenant?.tenant_name}"
                    )
                    android.util.Log.e(
                        "DUE_CYCLE_DEBUG",
                        """
Tenant = ${tenant?.tenant_name}

DueCycles = ${tenant?.dueCycles}

First Total = ${tenant?.dueCycles?.firstOrNull()?.totalAmount}

Rent = ${tenant?.rent}

Fixed Electricity = ${tenant?.fixed_electricity_amount}

Fixed Water = ${tenant?.fixed_waterbill_amount}
""".trimIndent()
                    )
                    RoomItem(
                        roomId = room.id,
                        roomNo = room.room_no,

                        propertyName = property.name,
                        propertyId = property.id,

                        tenantId = tenant?.id ?: "",
                        tenantName = tenant?.tenant_name ?: "Available for Rent",

                        phone = tenant?.phone_number ?: "",
                        profilePic = tenant?.profile_pic ?: "",

                        isOccupied = tenant != null,

                        roomStatus =
                            if (tenant != null) "Occupied"
                            else "Vacant",

                        monthlyRent =
                            tenant?.rent?.toDoubleOrNull() ?: 0.0,

                        securityAmount =
                            tenant?.room_deposit?.toDoubleOrNull() ?: 0.0,

                        rentStartDate =
                            tenant?.rent_start_date ?: "",

                        rentSettledTill =
                            tenant?.rent_receive_date ?: "",

                        nextDueDate =
                            tenant?.nextDueDate
                                ?.takeIf { it.isNotBlank() }
                                ?: tenant?.rent_end_date
                                ?: "",

                        advance =
                            tenant?.advance?.toDoubleOrNull() ?: 0.0,

                        paymentDue =
                            tenant?.payment_due?.toDoubleOrNull() ?: 0.0,

                        totalPayableThisCycle =
                            tenant?.dueCycles
                                ?.firstOrNull()
                                ?.totalAmount
                                ?: (tenant?.rent?.toDoubleOrNull() ?: 0.0),

                        fixedElectricity =
                            tenant?.fixed_electricity_amount
                                ?.toDoubleOrNull() ?: 0.0,

                        fixedWater =
                            tenant?.fixed_waterbill_amount
                                ?.toDoubleOrNull() ?: 0.0,

                        electricityMode =
                            tenant?.fixed_electricity ?: "",

                        waterMode =
                            tenant?.fixed_waterbill ?: "",

                        paymentMode =
                            tenant?.payment_mode ?: "",

                        meterReading =
                            tenant?.meter_reading ?: "",

                        waterReading =
                            tenant?.meter_reading_water ?: "",

                        lastMeterReading =
                            tenant?.last_meter_reading ?: "",

                        lastWaterReading =
                            tenant?.last_meter_reading_water ?: "",

                        costPerUnit =
                            tenant?.cost_per_unit ?: "",

                        costUnitWater =
                            tenant?.cost_unit_water ?: ""
                    )
                }
            )
        }
    }

    data class PropertyItem(
        val propertyName : String,
        val propertyId   : String,
        val rooms        : List<RoomItem>
    )

    data class RoomItem(
        val roomId          : String,
        val roomNo          : String,
        val propertyName    : String,
        val propertyId      : String,
        val tenantId        : String,
        val tenantName      : String,
        val phone           : String,
        val profilePic      : String,
        val isOccupied      : Boolean,
        val roomStatus      : String,
        val monthlyRent     : Double,
        val securityAmount  : Double,
        val rentStartDate   : String,
        val rentSettledTill : String,
        val nextDueDate     : String,
        val advance         : Double,
        val paymentDue      : Double,
        val totalPayableThisCycle : Double,
        val fixedElectricity : Double,
        val fixedWater : Double,
        val electricityMode : String,
        val waterMode : String,
        val paymentMode : String,
        val meterReading : String,
        val waterReading : String,
        val lastMeterReading : String,
        val lastWaterReading : String,
        val costPerUnit : String,
        val costUnitWater : String
    )
}