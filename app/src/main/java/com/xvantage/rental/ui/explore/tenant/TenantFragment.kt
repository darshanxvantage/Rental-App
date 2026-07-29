package com.xvantage.rental.ui.explore.tenant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.xvantage.rental.databinding.FragmentTenantBinding
import com.xvantage.rental.ui.explore.tenant.adapter.TenantAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TenantFragment : Fragment() {

    private var _binding: FragmentTenantBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TenantViewModel by viewModels()
    private lateinit var tenantAdapter: TenantAdapter

    companion object {
        fun newInstance() = TenantFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTenantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()

        viewModel.loadTenants()
    }

    override fun onResume() {
        super.onResume()
        // a tenant may have just been added from the old flow - refresh every visit
        viewModel.loadTenants()
    }

    private fun setupRecyclerView() {
        tenantAdapter = TenantAdapter(requireContext())
        binding.rvTenants.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tenantAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshTenants.setOnRefreshListener {
            viewModel.loadTenants()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tenants.collect { tenants ->
                tenantAdapter.setItems(tenants)
                binding.emptyTenantsLayout.visibility = if (tenants.isEmpty()) View.VISIBLE else View.GONE
                binding.rvTenants.visibility = if (tenants.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                binding.progressBarTenants.visibility = if (loading) View.VISIBLE else View.GONE
                binding.swipeRefreshTenants.isRefreshing = false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { message ->
                message?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}