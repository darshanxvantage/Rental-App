package com.xvantage.rental.ui.addProperty.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xvantage.rental.databinding.FragmentTenantsBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xvantage.rental.ui.dashboard.TenantListViewModel
import com.xvantage.rental.ui.dashboard.fragment.adapter.TenantsAdapter
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TenantsFragment : Fragment(){

    private var _binding: FragmentTenantsBinding? = null
    private val binding get() = _binding!!
    private val tenantViewModel: TenantListViewModel by activityViewModels()

    private lateinit var tenantsAdapter: TenantsAdapter
    private var propertyId: String = ""

    companion object {
        fun newInstance(propertyId: String): TenantsFragment {
            val fragment = TenantsFragment()
            val args = Bundle()
            args.putString("property_id", propertyId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            propertyId = it.getString("property_id", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTenantsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        tenantViewModel.loadTenants()

        observeTenants()
    }

    private fun setupRecyclerView() {

        tenantsAdapter =
            TenantsAdapter(
                requireContext()
            )

        binding.rvRooms.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvRooms.adapter =
            tenantsAdapter
    }


    private fun showEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvRooms.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvRooms.visibility = View.VISIBLE
        }
    }
    private fun observeTenants() {

        viewLifecycleOwner.lifecycleScope.launch {

            tenantViewModel.tenantList.collect { tenants ->

                tenants.forEach {

                    android.util.Log.e(
                        "TENANT_DEBUG",
                        "Tenant=${it.tenant_name} PropertyFk=${it.property_fk}"
                    )
                }

                val propertyTenants =

                    tenants.filter {

                        it.property_fk == propertyId
                    }

                android.util.Log.e(
                    "PROPERTY_TENANTS",
                    "Property=$propertyId Count=${propertyTenants.size}"
                )

                if (propertyTenants.isEmpty()) {

                    showEmptyState(true)

                } else {

                    showEmptyState(false)

                    tenantsAdapter.addItems(
                        propertyTenants
                    )
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}