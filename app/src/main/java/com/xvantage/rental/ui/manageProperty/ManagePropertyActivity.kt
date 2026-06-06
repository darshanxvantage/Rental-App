package com.xvantage.rental.ui.manageProperty

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.core.view.WindowCompat
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityManagePropertyBinding
import com.xvantage.rental.ui.addTenant.AddTenantActivity
import com.xvantage.rental.ui.manageProperty.adapter.ManagePropertyAdapter
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.CommonFunction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.xvantage.rental.ui.manageProperty.adapter.PropertyGroupAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.xvantage.rental.ui.dashboard.PropertyListViewModel

@AndroidEntryPoint
class ManagePropertyActivity : AppCompatActivity(), ManagePropertyAdapter.OnRoomItemClickListener {

    private lateinit var binding: ActivityManagePropertyBinding
    private lateinit var appPreference: AppPreference
    private lateinit var propertyGroupAdapter: PropertyGroupAdapter
    private val propertyViewModel: PropertyListViewModel by viewModels()

    // private val viewModel: ManagePropertyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_manage_property)
        appPreference = AppPreference(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set toolbar title
        binding.toolbar.tvTitle.setText(R.string.manage_property)

        initViews()
    }

    /**
     * Initialize RecyclerView and static data.
     */
    private fun initViews() {

        binding.rvPropertyList.layoutManager =
            LinearLayoutManager(this)

        propertyGroupAdapter =
            PropertyGroupAdapter(this, this)

        binding.rvPropertyList.adapter =
            propertyGroupAdapter

        propertyViewModel.loadProperties()

        lifecycleScope.launch {

            propertyViewModel.propertyList.collect { list ->

                propertyGroupAdapter.addItems(list)

            }
        }

        binding.toolbar.back.setOnClickListener { onBackPressed() }
    }

    override fun onRoomClick(roomNumber: String, position: Int) {
        CommonFunction().toast(this, "$roomNumber Clicked")
    }

    override fun onAddTenantClick(roomNumber: String, position: Int) {
        CommonFunction().navigation(this, AddTenantActivity::class.java)
    }
}
