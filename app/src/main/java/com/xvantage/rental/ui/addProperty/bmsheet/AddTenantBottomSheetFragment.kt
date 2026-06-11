package com.xvantage.rental.ui.addProperty.bmsheet

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xvantage.rental.databinding.FragmentAddTenantBottomSheetBinding
import com.xvantage.rental.ui.addProperty.tempFiles.Room
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import android.widget.ArrayAdapter
import com.google.gson.Gson
import com.xvantage.rental.network.response.PropertyDetailsData

class AddTenantBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAddTenantBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var propertyDetails: PropertyDetailsData? = null

    private var onTenantAddedListener: ((Tenant) -> Unit)? = null
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Hidden IDs managed in code
    var roomId: String = ""
    var propertyId: String = ""

    fun setOnTenantAddedListener(listener: (Tenant) -> Unit) {
        onTenantAddedListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTenantBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupDatePickers()
        setupActionButtons()

        loadRoomsFromArguments()
    }



    private fun setupDatePickers() {
        binding.etRentStartDate.setOnClickListener { showDatePicker { date -> binding.etRentStartDate.setText(date) } }
        binding.etRentSubmissionDate.setOnClickListener { showDatePicker { date -> binding.etRentSubmissionDate.setText(date) } }
    }

    private fun showDatePicker(onDateSet: (String) -> Unit) {
        val listener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day)
            onDateSet(dateFormatter.format(calendar.time))
        }
        DatePickerDialog(
            requireContext(), listener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupActionButtons() {
        binding.ivClose.setOnClickListener { dismiss() }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSave.setOnClickListener {
            if (validateFields()) saveTenant()
        }
    }

    private fun loadRoomsFromArguments() {

        val json =
            arguments?.getString(
                "property_json"
            ) ?: return

        propertyDetails =
            Gson().fromJson(
                json,
                PropertyDetailsData::class.java
            )

        val roomList =

            propertyDetails?.rooms
                ?.sortedBy {

                    if (
                        it.status.equals(
                            "OCCUPED",
                            true
                        )
                    ) 1 else 0
                }
                ?.map {

                    val roomStatus =

                        if (
                            it.status.equals(
                                "OCCUPED",
                                true
                            )
                        ) {
                            "Occupied"
                        } else {
                            "Vacant"
                        }

                    "Room ${it.room_no} - $roomStatus"
                }

                ?: emptyList()

        android.util.Log.e(
            "ROOM_SPINNER",
            roomList.toString()
        )

        val adapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                roomList
            )

        binding.spinnerRoomName.adapter =
            adapter
    }

    private fun validateFields(): Boolean {
        var valid = true
        if (binding.etTenantName.text.isNullOrBlank()) {

            binding.etTenantName.error =
                "Tenant name required"

            valid = false
        }

        if (binding.spinnerRoomName.selectedItem == null) {

            valid = false
        }
        return valid
    }

    private fun saveTenant() {
        val tenant = Tenant(
            id = UUID.randomUUID().toString(),
            roomId = roomId,
            propertyId = propertyId,
            roomName =
                binding.spinnerRoomName
                    .selectedItem
                    ?.toString()
                    ?: "",
            tenantName = binding.etTenantName.text.toString(),
            aadhaarPhotoUri = "binding.ivAadharUpload.drawable?.toString()",
            tenantPhotoUri = "binding.ivTenantPhotoUpload.drawable?.toUri().toString()",
            rentStartDate = binding.etRentStartDate.text.toString(),
            roomDeposit = binding.etRoomDeposit.text.toString().toDoubleOrNull() ?: 0.0,
            rentSubmissionDate = binding.etRentSubmissionDate.text.toString()
        )
        onTenantAddedListener?.invoke(tenant)
        dismiss()
    }

    fun addRoom(room: Room) {
        // Add new room to the list
//        rooms.add(room)
//        roomAdapter.submitList(rooms.toList())
//
//         Hide empty state if this is the first room
//        if (rooms.size == 1) {
//            showEmptyState(false)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Data model
data class Tenant(
    val id: String,
    val roomId: String,
    val propertyId: String,
    val roomName: String,
    val tenantName: String,
    val aadhaarPhotoUri: String,
    val tenantPhotoUri: String,
    val rentStartDate: String,
    val roomDeposit: Double,
    val rentSubmissionDate: String
)
