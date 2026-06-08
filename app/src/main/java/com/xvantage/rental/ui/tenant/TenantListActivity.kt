package com.xvantage.rental.ui.tenant

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.xvantage.rental.databinding.ActivityTenantListBinding
import com.xvantage.rental.ui.dashboard.TenantListViewModel
import com.xvantage.rental.ui.dashboard.fragment.adapter.TenantsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TenantListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTenantListBinding

    private val viewModel: TenantListViewModel by viewModels()

    private lateinit var tenantsAdapter: TenantsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityTenantListBinding.inflate(layoutInflater)

        setContentView(binding.root)

        tenantsAdapter =
            TenantsAdapter(this)

        binding.rvTenants.layoutManager =
            GridLayoutManager(this, 2)

        binding.rvTenants.adapter =
            tenantsAdapter

        observeData()

        viewModel.loadTenants()
    }

    private fun observeData() {

        lifecycleScope.launch {

            viewModel.tenantList.collect { list ->

                tenantsAdapter.addItems(list)
            }
        }
    }
}