package com.xvantage.rental.ui.explore.createListing.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.xvantage.rental.databinding.FragmentHeroWelcomeBinding
import com.xvantage.rental.ui.explore.createListing.CreateListingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HeroWelcomeFragment : Fragment() {

    private var _binding: FragmentHeroWelcomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateListingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHeroWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnGetStarted.setOnClickListener {
            viewModel.dismissHero()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HeroWelcomeFragment()
    }
}