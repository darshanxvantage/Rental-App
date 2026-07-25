package com.xvantage.rental.ui.explore.createListing.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.xvantage.rental.databinding.FragmentSharingPriceBinding
import com.xvantage.rental.ui.explore.createListing.CreateListingViewModel
import com.xvantage.rental.ui.explore.createListing.WizardStepFragment
import com.xvantage.rental.ui.explore.createListing.adapter.SharingPriceAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SharingPriceFragment : Fragment(), WizardStepFragment {

    private var _binding: FragmentSharingPriceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateListingViewModel by activityViewModels()
    private lateinit var sharingPriceAdapter: SharingPriceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSharingPriceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharingPriceAdapter = SharingPriceAdapter(viewModel.formState.value.sharingPrices)
        binding.rvSharingPriceRows.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sharingPriceAdapter
        }

        binding.btnAddSharingRow.setOnClickListener {
            sharingPriceAdapter.addEmptyRow()
        }
    }

    override fun validateAndSave(): Boolean {
        val rows = sharingPriceAdapter.getAllRows()
        val validRows = rows.filter { it.price > 0 && it.availableBeds >= 0 }

        if (validRows.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one valid sharing-type price", Toast.LENGTH_SHORT).show()
            return false
        }

        val invalidRow = rows.any { it.price <= 0 }
        if (invalidRow) {
            Toast.makeText(requireContext(), "Please enter a valid price for every sharing type", Toast.LENGTH_SHORT).show()
            return false
        }

        viewModel.updateForm { copy(sharingPrices = validRows) }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SharingPriceFragment()
    }
}