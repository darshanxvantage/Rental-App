package com.xvantage.rental.ui.takeRent.activity

import android.app.LauncherActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.xvantage.rental.R
import com.xvantage.rental.utils.AppPreference
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.xvantage.rental.databinding.ActivityTakeRentBinding
import com.xvantage.rental.ui.takeRent.adapter.PropertyRoomAdapter
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
        layoutBinding.toolbar.back.setOnClickListener {
            onBackPressed()
        }
        layoutBinding.rvPropertyList.layoutManager = LinearLayoutManager(this)


        viewModel.loadProperties()

        lifecycleScope.launch {

            viewModel.propertyList.collect { properties ->

                val propertyData =

                    properties.map { property ->

                        PropertyItem(

                            property.name,

                            property.property_room_no.map { room ->

                                android.util.Log.e(
                                    "ROOM_STATUS",
                                    "Room=${room.room_no}, Status=${room.status}"
                                )

                                RoomItem(

                                    room.room_no,

                                    property.address,

                                    0.0,

                                    0.0,
                                    room.status.equals("OCCUPED", true)

                                )
                            }
                        )
                    }

                layoutBinding.rvPropertyList.adapter =
                    PropertyRoomAdapter(
                        propertyData,
                        this@TakeRentActivity
                    )
            }
            onClickEvents()
        }
    }

    private fun onClickEvents() {
        layoutBinding.toolbar.back.setOnClickListener {
            finish()
        }
    }

    data class PropertyItem(val propertyName: String,val rooms: List<RoomItem>) : LauncherActivity.ListItem()

    data class RoomItem(
        val roomId: String,
        val address: String,
        val monthlyRent: Double,
        val securityAmount: Double,

        val occupied: Boolean = false,
        val tenantName: String = ""
    ) : LauncherActivity.ListItem()
}