package com.xvantage.rental.ui.explore.createListing.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xvantage.rental.databinding.FragmentCelebrationBinding
import com.xvantage.rental.ui.explore.createListing.CreateListingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CelebrationFragment : Fragment() {

    private var _binding: FragmentCelebrationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCelebrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val hostActivity = activity as? CreateListingActivity
        if (hostActivity?.isOnboardingFlow() == false) {
            binding.btnCelebrationDone.text = "Done"
        }

        binding.btnCelebrationDone.setOnClickListener {
            hostActivity?.finishWizard()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = CelebrationFragment()
    }
}