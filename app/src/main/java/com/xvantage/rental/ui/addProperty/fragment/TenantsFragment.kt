package com.xvantage.rental.ui.addProperty.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.xvantage.rental.databinding.FragmentTenantsBinding
import com.xvantage.rental.ui.dashboard.TenantListViewModel
import com.xvantage.rental.ui.dashboard.fragment.adapter.TenantsAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TenantsFragment : Fragment() {

    private var _binding: FragmentTenantsBinding? = null
    private val binding get() = _binding!!

    private val tenantViewModel: TenantListViewModel by activityViewModels()

    private lateinit var tenantsAdapter: TenantsAdapter

    private var propertyId: String = ""

    companion object {

        private const val ARG_PROPERTY_ID = "property_id"

        fun newInstance(propertyId: String): TenantsFragment {

            return TenantsFragment().apply {

                arguments = Bundle().apply {

                    putString(
                        ARG_PROPERTY_ID,
                        propertyId
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        propertyId =
            arguments?.getString(ARG_PROPERTY_ID)
                .orEmpty()

        Log.d(
            "TENANTS_FRAGMENT",
            "Property ID = $propertyId"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentTenantsBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        setupRecyclerView()

        observeTenants()

        // First API load
        tenantViewModel.loadTenants()
    }


    override fun onResume() {
        super.onResume()

        Log.d(
            "TENANTS_FRAGMENT",
            "onResume -> Reloading tenant list"
        )

        tenantViewModel.loadTenants()
    }

    fun refreshTenantList() {

        tenantViewModel.loadTenants()
    }

    private fun setupRecyclerView() {

        tenantsAdapter =
            TenantsAdapter(
                requireContext(),
                isGridMode = true
            )

        binding.rvRooms.layoutManager =
            GridLayoutManager(
                requireContext(),
                2
            )

        binding.rvRooms.adapter =
            tenantsAdapter
    }

    private fun observeTenants() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                tenantViewModel.tenantList.collect { tenants ->

                    Log.d(
                        "TENANTS_FRAGMENT",
                        "Total tenants from API = ${tenants.size}"
                    )

                    tenants.forEach { tenant ->

                        Log.d(
                            "TENANT_DEBUG",
                            "Name=${tenant.tenant_name}, " +
                                    "PropertyFk=${tenant.property_fk}, " +
                                    "CurrentProperty=$propertyId"
                        )
                    }

                    val propertyTenants =
                        tenants.filter { tenant ->

                            tenant.property_fk
                                .trim()
                                .equals(
                                    propertyId.trim(),
                                    ignoreCase = true
                                )
                        }

                    Log.d(
                        "PROPERTY_TENANTS",
                        "Property=$propertyId, " +
                                "Count=${propertyTenants.size}"
                    )

                    tenantsAdapter.addItems(
                        propertyTenants
                    )

                    showEmptyState(
                        propertyTenants.isEmpty()
                    )
                }
            }
        }
    }

    private fun showEmptyState(
        isEmpty: Boolean
    ) {

        binding.emptyState.visibility =
            if (isEmpty) {
                View.VISIBLE
            } else {
                View.GONE
            }

        binding.rvRooms.visibility =
            if (isEmpty) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}