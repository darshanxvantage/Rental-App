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
import com.xvantage.rental.R
import com.xvantage.rental.network.response.TenantItem
import androidx.appcompat.widget.SearchView
import android.view.Menu


@AndroidEntryPoint
class TenantListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTenantListBinding
    private var originalList = mutableListOf<TenantItem>()

    private val viewModel: TenantListViewModel by viewModels()

    private lateinit var tenantsAdapter: TenantsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityTenantListBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {

            onBackPressedDispatcher.onBackPressed()
        }


        tenantsAdapter =
            TenantsAdapter(this)



        binding.rvTenants.layoutManager = GridLayoutManager(this, 2)

        binding.rvTenants.adapter =
            tenantsAdapter


        binding.rvTenants.setHasFixedSize(true)

        observeData()

        viewModel.loadTenants()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(
            R.menu.menu_tenant_list,
            menu
        )

        val searchItem =
            menu?.findItem(R.id.action_search)

        val searchView =
            searchItem?.actionView as? SearchView
                ?: return true

        searchView.queryHint =
            "Search Tenant or Property"

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(
                    query: String?
                ): Boolean {
                    return false
                }

                override fun onQueryTextChange(
                    newText: String?
                ): Boolean {

                    filterTenants(
                        newText ?: ""
                    )

                    return true
                }
            }
        )

        return true
    }

    private fun observeData() {

        lifecycleScope.launch {

            viewModel.tenantList.collect { list ->

                originalList.clear()

                originalList.addAll(list)

                tenantsAdapter.addItems(list)

                binding.tvEmpty.visibility =
                    if (list.isEmpty())
                        android.view.View.VISIBLE
                    else
                        android.view.View.GONE
            }
        }
    }
    private fun filterTenants(
        query: String
    ) {

        if (query.isBlank()) {

            tenantsAdapter.addItems(
                originalList
            )

            binding.tvEmpty.visibility =
                if (originalList.isEmpty())
                    android.view.View.VISIBLE
                else
                    android.view.View.GONE

            return
        }

        val filteredList =
            originalList.filter {

                it.tenant_name.contains(
                    query,
                    ignoreCase = true
                )

                        ||

                        it.tenant_details
                            ?.property
                            ?.name
                            ?.contains(
                                query,
                                ignoreCase = true
                            ) == true
            }

        tenantsAdapter.addItems(
            filteredList
        )

        binding.tvEmpty.visibility =
            if (filteredList.isEmpty())
                android.view.View.VISIBLE
            else
                android.view.View.GONE
    }

}