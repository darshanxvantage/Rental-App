package com.xvantage.rental.ui.auth.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.xvantage.rental.databinding.FragmentCreateProfileBinding
import com.xvantage.rental.ui.auth.AuthViewModel
import com.xvantage.rental.ui.auth.fragment.sealed.AuthState
import com.xvantage.rental.ui.dashboard.DashboardActivity
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.StateProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class CreateProfileFragment : Fragment() {

    private lateinit var binding: FragmentCreateProfileBinding
    private lateinit var appPreference: AppPreference
    private val viewModel: AuthViewModel by activityViewModels()

    // ── Location resolution popup (system "Turn on Location" dialog) launcher ──
    private val locationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // User ne GPS ON kari didhu → have location fetch kar
            requestFreshLocation()
        } else {
//            Toast.makeText(requireContext(), "Please turn on location to auto-fill address", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Runtime permission launcher (Fine Location) ──
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            checkLocationSettingsAndFetch()
        } else {
//            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.resetAuthState()
        appPreference = AppPreference(requireContext())

        setupStateDropdown()
        setupGenderDropdown()

        // GPS ICON CLICK
        binding.ivGpsIcon.setOnClickListener {
            fetchCurrentLocation()
        }

        // CREATE PROFILE BUTTON
        binding.btnCreateProfile.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val gender = binding.etGender.text.toString().trim()
            val state = binding.etState.text.toString().trim()
            val city = binding.etCity.text.toString().trim()

            binding.etFirstName.error = null
            binding.etLastName.error = null
            binding.etEmail.error = null
            binding.etCity.error = null

            when {
                firstName.isEmpty() -> {
                    binding.etFirstName.error = "⚠ First name is required"
                    binding.etFirstName.requestFocus()
                }
                lastName.isEmpty() -> {
                    binding.etLastName.error = "⚠ Last name is required"
                    binding.etLastName.requestFocus()
                }
                email.isEmpty() -> {
                    binding.etEmail.error = "⚠ Email is required"
                    binding.etEmail.requestFocus()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    binding.etEmail.error = "⚠ Enter a valid email"
                    binding.etEmail.requestFocus()
                }
                gender.isEmpty() -> {
                    Toast.makeText(context, "⚠ Please select your gender", Toast.LENGTH_SHORT).show()
                }
                state.isEmpty() -> {
                    Toast.makeText(context, "⚠ Please select your state", Toast.LENGTH_SHORT).show()
                }
                city.isEmpty() -> {
                    binding.etCity.error = "⚠ City is required"
                    binding.etCity.requestFocus()
                }
                else -> {
                    viewModel.createProfile(firstName, lastName, email, state, city, gender)
                }
            }
        }

        observeState()
    }

    private fun setupStateDropdown() {
        val stateList = StateProvider.getStates(requireContext())
        if (stateList.isEmpty()) {
//            Toast.makeText(requireContext(), "State list not loaded", Toast.LENGTH_SHORT).show()
            return
        }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            stateList
        )
        binding.etState.setAdapter(adapter)
        binding.etState.threshold = 1
        binding.etState.keyListener = null
        binding.etState.setOnClickListener { binding.etState.showDropDown() }
        binding.etState.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.etState.showDropDown()
        }
        binding.etState.setOnTouchListener { _, _ ->
            binding.etState.showDropDown()
            false
        }
    }

    private fun setupGenderDropdown() {
        val genders = listOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            genders
        )
        binding.etGender.setAdapter(adapter)
        binding.etGender.keyListener = null
        binding.etGender.setOnClickListener { binding.etGender.showDropDown() }
        binding.tilGender.setEndIconOnClickListener { binding.etGender.showDropDown() }
    }


    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        checkLocationSettingsAndFetch()
    }

    private fun checkLocationSettingsAndFetch() {
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10000
        ).build()

        val settingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        val settingsClient = LocationServices.getSettingsClient(requireActivity())

        settingsClient.checkLocationSettings(settingsRequest)
            .addOnSuccessListener {
                // GPS already ON → fetch location
                requestFreshLocation()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // GPS OFF → show system "Allow" popup to turn it on
                    try {
                        val intentSenderRequest =
                            IntentSenderRequest.Builder(exception.resolution).build()
                        locationSettingsLauncher.launch(intentSenderRequest)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Toast.makeText(requireContext(), "Unable to open location settings", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Location services unavailable on this device", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun requestFreshLocation() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        Toast.makeText(requireContext(), "Fetching location...", Toast.LENGTH_SHORT).show()

        fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                getAddressFromLocation(location.latitude, location.longitude)
            } else {
                // lastLocation cache empty (fresh device/emulator) → actively request one
                fetchCurrentLocationActively(fusedClient)
            }
        }.addOnFailureListener {
            fetchCurrentLocationActively(fusedClient)
        }
    }

    private fun fetchCurrentLocationActively(
        fusedClient: com.google.android.gms.location.FusedLocationProviderClient
    ) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val cancellationTokenSource = CancellationTokenSource()
        val currentLocationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .build()

        fusedClient.getCurrentLocation(currentLocationRequest, cancellationTokenSource.token)
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    getAddressFromLocation(location.latitude, location.longitude)
                } else {
//                    Toast.makeText(
//                        requireContext(),
//                        "Location not found. Please move to an open area and try again",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            }
            .addOnFailureListener {
//                Toast.makeText(requireContext(), "Location not found, try again", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getAddressFromLocation(lat: Double, lng: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, lng, 1) { addresses ->
                requireActivity().runOnUiThread {
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        binding.etState.setText(address.adminArea ?: "")
                        binding.etCity.setText(address.locality ?: address.subAdminArea ?: "")
//                        Toast.makeText(requireContext(), "Location filled ✅", Toast.LENGTH_SHORT).show()
                    } else {
//                        Toast.makeText(requireContext(), "Unable to detect address, please enter manually", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                binding.etState.setText(address.adminArea ?: "")
                binding.etCity.setText(address.locality ?: address.subAdminArea ?: "")
//                Toast.makeText(requireContext(), "Location filled ✅", Toast.LENGTH_SHORT).show()
            } else {
//ProfileFragment.kt
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.authState.collect {
                when (it) {
                    is AuthState.Success -> {
                        appPreference.setUserName("${binding.etFirstName.text} ${binding.etLastName.text}")
                        appPreference.setEmail(binding.etEmail.text.toString())
                        appPreference.setCity(binding.etCity.text.toString())
                        appPreference.setState(binding.etState.text.toString())
                        appPreference.setGender(binding.etGender.text.toString())

                        Toast.makeText(context, "Profile Created Successfully", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(requireContext(), DashboardActivity::class.java))
                        requireActivity().finish()
                    }
                    is AuthState.Error -> {
                        Toast.makeText(context, it.error, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}