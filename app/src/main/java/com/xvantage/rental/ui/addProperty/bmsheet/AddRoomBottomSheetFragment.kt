package com.xvantage.rental.ui.addProperty.bmsheet

import android.R
import android.app.DatePickerDialog
import android.graphics.Color
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class AddRoomBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAddRoomBottomSheetBinding? = null
    private val binding get() = _binding!!
    private var propertyId = ""
    private var propertyTypeId = ""

    private val viewModel: RoomViewModel by viewModels()

    private var onRoomAddedListener: ((Room) -> Unit)? = null
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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

        android.util.Log.e("ROOM_PROPERTY_ID", propertyId)
        android.util.Log.e("ROOM_PROPERTY_TYPE_ID", propertyTypeId)

        setupRoomTypeSpinner()
        setupDatePicker()
        setupActionButtons()
        observeState()
    }

    private fun setupRoomTypeSpinner() {
        val roomTypes = arrayOf("1BHK", "2BHK", "3BHK", "Single Room", "Studio", "Other")
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_dropdown_item,
            roomTypes
        )
        binding.spinnerRoomType.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.etReadingDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {

        val dateListener =
            DatePickerDialog.OnDateSetListener { _, year, month, day ->

                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                updateDateField()
            }


        val datePickerDialog = DatePickerDialog(
            requireContext(),
            dateListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )


        datePickerDialog.setOnShowListener {

            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                .setTextColor(Color.parseColor("#1565C0"))

            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                .setTextColor(Color.parseColor("#1565C0"))
        }


        datePickerDialog.show()
    }

    private fun updateDateField() {
        binding.etReadingDate.setText(dateFormatter.format(calendar.time))
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

        // Validate room number/name
        if (binding.etRoomNumber.text.isNullOrBlank()) {
            binding.etRoomNumber.error = "Room number is required"
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

            // ✅ FIX: actual UUID instead of hardcoded "1"
            propertyTypeId = propertyTypeId,

            roomNo = binding.etRoomNumber.text.toString(),

            roomTypeId = "",

            roomTypeText = binding.spinnerRoomType.selectedItem.toString(),

            address = "",

            rent = binding.etRoomRent.text.toString(),

            meterReading = binding.etMeterReading.text.toString(),

            meterReadingLastDate = binding.etReadingDate.text.toString(),

            roomImage = null
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
                                "Room Created Successfully",
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