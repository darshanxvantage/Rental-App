package com.xvantage.rental.ui.explore.createListing.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.LocationServices
import com.xvantage.rental.databinding.FragmentBasicDetailsBinding
import com.xvantage.rental.ui.explore.createListing.CreateListingViewModel
import com.xvantage.rental.ui.explore.createListing.WizardStepFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BasicDetailsFragment : Fragment(), WizardStepFragment {

    private var _binding: FragmentBasicDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateListingViewModel by activityViewModels()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchCurrentLocation() else Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBasicDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefillFromState()
        binding.btnUseCurrentLocation.setOnClickListener { requestLocation() }
    }

    private fun prefillFromState() {
        val form = viewModel.formState.value
        binding.etTitle.setText(form.title)
        binding.etDescription.setText(form.description)
        binding.etCity.setText(form.city)
        binding.etLocality.setText(form.locality)
        binding.etAddress.setText(form.address)
        binding.etLandmark.setText(form.landmark)
        binding.etContactNumber.setText(form.contactNumber)
        binding.etWhatsappNumber.setText(form.whatsappNumber)
        binding.etAlternateNumber.setText(form.alternateNumber)
        if (form.totalBeds > 0) binding.etTotalBeds.setText(form.totalBeds.toString())
        if (form.availableBeds > 0) binding.etAvailableBeds.setText(form.availableBeds.toString())
        form.minStayMonths?.let { binding.etMinStayMonths.setText(it.toString()) }
        form.bookingAmount?.let { binding.etBookingAmount.setText(it.toInt().toString()) }

        when (form.genderPreference) {
            "male" -> binding.chipGenderMaleWizard.isChecked = true
            "female" -> binding.chipGenderFemaleWizard.isChecked = true
            else -> binding.chipGenderUnisexWizard.isChecked = true
        }
        when (form.occupancyFor) {
            "student" -> binding.chipOccupancyStudentWizard.isChecked = true
            "working_professional" -> binding.chipOccupancyProfessionalWizard.isChecked = true
            else -> binding.chipOccupancyAnyWizard.isChecked = true
        }
    }

    private fun requestLocation() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            fetchCurrentLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @Suppress("MissingPermission")
    private fun fetchCurrentLocation() {
        val client = LocationServices.getFusedLocationProviderClient(requireActivity())
        client.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.updateForm { copy(latitude = location.latitude, longitude = location.longitude) }
                    Toast.makeText(requireContext(), "Location captured ✓", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Could not fetch current location. Try again outdoors.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Location fetch failed", Toast.LENGTH_SHORT).show()
            }
    }

    override fun validateAndSave(): Boolean {
        val title = binding.etTitle.text?.toString()?.trim().orEmpty()
        val city = binding.etCity.text?.toString()?.trim().orEmpty()
        val locality = binding.etLocality.text?.toString()?.trim().orEmpty()
        val address = binding.etAddress.text?.toString()?.trim().orEmpty()
        val contactNumber = binding.etContactNumber.text?.toString()?.trim().orEmpty()
        val totalBeds = binding.etTotalBeds.text?.toString()?.toIntOrNull()
        val availableBeds = binding.etAvailableBeds.text?.toString()?.toIntOrNull()

        if (title.isEmpty()) return showError("Please enter a title")
        if (city.isEmpty()) return showError("Please enter the city")
        if (locality.isEmpty()) return showError("Please enter the locality/area")
        if (address.isEmpty()) return showError("Please enter the full address")
        if (contactNumber.length != 10) return showError("Please enter a valid 10-digit contact number")
        if (totalBeds == null || totalBeds <= 0) return showError("Please enter total beds")
        if (availableBeds == null || availableBeds < 0) return showError("Please enter available beds")
        if (availableBeds > totalBeds) return showError("Available beds cannot exceed total beds")

        val genderPreference = when (binding.genderChipGroupWizard.checkedChipId) {
            binding.chipGenderMaleWizard.id -> "male"
            binding.chipGenderFemaleWizard.id -> "female"
            else -> "unisex"
        }
        val occupancyFor = when (binding.occupancyChipGroupWizard.checkedChipId) {
            binding.chipOccupancyStudentWizard.id -> "student"
            binding.chipOccupancyProfessionalWizard.id -> "working_professional"
            else -> "any"
        }

        viewModel.updateForm {
            copy(
                title = title,
                description = binding.etDescription.text?.toString()?.trim().orEmpty(),
                city = city,
                locality = locality,
                address = address,
                landmark = binding.etLandmark.text?.toString()?.trim().orEmpty(),
                contactNumber = contactNumber,
                whatsappNumber = binding.etWhatsappNumber.text?.toString()?.trim().orEmpty(),
                alternateNumber = binding.etAlternateNumber.text?.toString()?.trim().orEmpty(),
                totalBeds = totalBeds,
                availableBeds = availableBeds,
                minStayMonths = binding.etMinStayMonths.text?.toString()?.toIntOrNull(),
                bookingAmount = binding.etBookingAmount.text?.toString()?.toDoubleOrNull(),
                genderPreference = genderPreference,
                occupancyFor = occupancyFor
            )
        }
        return true
    }

    private fun showError(message: String): Boolean {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = BasicDetailsFragment()
    }
}