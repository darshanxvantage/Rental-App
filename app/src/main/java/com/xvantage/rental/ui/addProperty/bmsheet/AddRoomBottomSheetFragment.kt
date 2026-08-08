package com.xvantage.rental.ui.addProperty.bmsheet

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xvantage.rental.databinding.FragmentAddRoomBottomSheetBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.xvantage.rental.ui.addProperty.RoomViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.xvantage.rental.ui.addProperty.tempFiles.Room
import com.xvantage.rental.utils.UnitLabelProvider

@AndroidEntryPoint
class AddRoomBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAddRoomBottomSheetBinding? = null
    private val binding get() = _binding!!
    private var propertyId = ""
    private var propertyTypeId = ""
    private var propertyTypeName = ""
    private var existingRoomNumbers: List<String> = emptyList()

    private val viewModel: RoomViewModel by viewModels()

    private var onRoomAddedListener: ((Room) -> Unit)? = null

    fun setOnRoomAddedListener(listener: (Room) -> Unit) {
        onRoomAddedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRoomBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        propertyId = arguments?.getString("propertyId") ?: ""
        propertyTypeId = arguments?.getString("propertyTypeId") ?: ""  // ✅ get UUID
        propertyTypeName = arguments?.getString("propertyTypeName") ?: ""
        existingRoomNumbers = arguments?.getStringArrayList("existingRoomNumbers") ?: emptyList()

        android.util.Log.e("ROOM_PROPERTY_ID", propertyId)
        android.util.Log.e("ROOM_PROPERTY_TYPE_ID", propertyTypeId)

        setupRoomTypeSpinner()
        updateLabelsForPropertyType()
        setupActionButtons()
        observeState()
    }

    private fun setupRoomTypeSpinner() {
        val roomTypes = when {
            propertyTypeName.contains("pg", true) -> arrayOf("Single", "2-Sharing", "3-Sharing", "4-Sharing")
            propertyTypeName.contains("apartment", true) || propertyTypeName.contains("flat", true) -> arrayOf("1BHK", "2BHK", "3BHK", "Single Room", "Studio")
            propertyTypeName.contains("commercial", true) || propertyTypeName.contains("shop", true) -> arrayOf("Shop")
            propertyTypeName.contains("office", true) -> arrayOf("Office")
            propertyTypeName.contains("row house", true) -> arrayOf("Floor")
            else -> arrayOf("Room")
        }
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_dropdown_item,
            roomTypes
        )
        binding.spinnerRoomType.adapter = adapter
    }

    private fun unitLabel() = UnitLabelProvider.forPropertyType(propertyTypeName).singular

    private fun updateLabelsForPropertyType() {
        val label = unitLabel()
        binding.tvSheetTitle.text = "Add $label"
        binding.tvUnitNumberLabel.text = "$label Number/Name"
        binding.etRoomNumber.hint = "Enter $label Number/Name"
        binding.tvUnitTypeLabel.text = "$label Type"
        binding.tvUnitRentLabel.text = "$label Rent"
        binding.btnSave.text = "Save $label"
    }

    private fun setupActionButtons() {
        // Cancel button
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        android.util.Log.e("ROOM_DEBUG", "Save button clicked")
        // Save button
        binding.btnSave.setOnClickListener {
            if (validateFields()) {
                saveRoom()
            }
        }

        // Close button
        binding.ivClose.setOnClickListener {
            dismiss()
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        val enteredRoomNumber = binding.etRoomNumber.text?.toString()?.trim() ?: ""


        if (enteredRoomNumber.isBlank()) {
            binding.etRoomNumber.error = "${unitLabel()} number is required"
            isValid = false
        } else if (existingRoomNumbers.any { it.trim().equals(enteredRoomNumber, ignoreCase = true) }) {

            binding.etRoomNumber.error = "This number already exists in this property"
            isValid = false
        }

        // Validate rent
        if (binding.etRoomRent.text.isNullOrBlank()) {
            binding.etRoomRent.error = "Rent amount is required"
            isValid = false
        }

        return isValid
    }

    private fun saveRoom() {

        viewModel.createRoom(

            propertyId = propertyId,


            propertyTypeId = propertyTypeId,

            roomNo = binding.etRoomNumber.text.toString(),

            roomTypeId = "",

            roomTypeText = binding.spinnerRoomType.selectedItem.toString(),

            address = "",

            rent = binding.etRoomRent.text.toString(),

            meterReading = "",

            meterReadingLastDate = "",

            roomImage = null,
            sharingType = if (propertyTypeName.contains("pg", true)) binding.spinnerRoomType.selectedItem.toString() else null,
            bedCount = if (propertyTypeName.contains("pg", true)) (binding.spinnerRoomType.selectedItem.toString().substringBefore("-").toIntOrNull() ?: 1).toString() else null,
            floorLabel = if (propertyTypeName.contains("row house", true)) binding.etRoomNumber.text.toString() else null
        )
    }

    private fun observeState() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                androidx.lifecycle.Lifecycle.State.STARTED
            ) {

                viewModel.state.collect { state ->

                    when (state) {

                        is RoomViewModel.State.Loading -> {
                            // Optional: ProgressBar બતાવો
                        }

                        is RoomViewModel.State.Success -> {

                            android.widget.Toast.makeText(
                                requireContext(),
                                "${unitLabel()} created successfully",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()

                            // Notify parent activity to reload property details
                            parentFragmentManager.setFragmentResult(
                                "room_added",
                                Bundle()
                            )

                            dismiss()
                        }

                        is RoomViewModel.State.Error -> {

                            android.widget.Toast.makeText(
                                requireContext(),
                                state.message,
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}