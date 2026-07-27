package com.xvantage.rental.ui.explore.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.xvantage.rental.databinding.FragmentExploreProfileBinding
import com.xvantage.rental.ui.auth.AuthActivity
import com.xvantage.rental.ui.dashboard.DashboardActivity
import com.xvantage.rental.ui.explore.common.ExploreRoleManager
import com.xvantage.rental.ui.explore.myListings.MyListingsActivity
import com.xvantage.rental.utils.AppPreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Explore-side "Profile" tab (4th bottom nav item). Mirrors the old
 * Dashboard's Profile screen in spirit: shows who's logged in, gives
 * access to My Listings, a conditional "Switch to Owner" (only for users
 * who already have at least one listing), and Logout.
 */
@AndroidEntryPoint
class ExploreProfileFragment : Fragment() {

    private var _binding: FragmentExploreProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExploreProfileViewModel by viewModels()
    private lateinit var appPreference: AppPreference

    companion object {
        fun newInstance() = ExploreProfileFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appPreference = AppPreference(requireContext())

        binding.tvExploreUserName.text = appPreference.getUserName() ?: "User"
        binding.tvExploreUserPhone.text = appPreference.getPhone() ?: ""

        binding.cardMyListings.setOnClickListener {
            startActivity(Intent(requireContext(), MyListingsActivity::class.java))
        }

        binding.cardSwitchToOwner.setOnClickListener {
            ExploreRoleManager.saveRole(requireContext(), ExploreRoleManager.ROLE_OWNER)
            startActivity(Intent(requireContext(), DashboardActivity::class.java))
            requireActivity().finish()
        }

        binding.cardLogout.setOnClickListener {
            appPreference.logoutUser()
            ExploreRoleManager.clear(requireContext())
            startActivity(Intent(requireContext(), AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            requireActivity().finish()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.hasListing.collect { hasListing ->
                binding.cardSwitchToOwner.visibility =
                    if (hasListing == true) View.VISIBLE else View.GONE
            }
        }

        viewModel.checkHasListing()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}