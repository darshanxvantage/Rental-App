package com.xvantage.rental.ui.auth.fragment

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.xvantage.rental.R
import com.xvantage.rental.databinding.FragmentCreateProfileBinding
import com.xvantage.rental.ui.auth.AuthViewModel
import com.xvantage.rental.ui.auth.fragment.sealed.AuthState
import com.xvantage.rental.ui.dashboard.DashboardActivity
import com.xvantage.rental.utils.AppPreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class CreateProfileFragment : Fragment() {

    private lateinit var binding: FragmentCreateProfileBinding
    private lateinit var appPreference: AppPreference
    private val viewModel: AuthViewModel by activityViewModels()

    // Date of birth chosen by the user, kept in yyyy-MM-dd (ISO) for the API.
    // etDob itself only ever shows the pretty "dd MMM, yyyy" version.
    private var selectedDobIso: String? = null

    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

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

        setupGenderDropdown()
        setupDobPicker()
        setupNameCapitalization()
        setupInlineErrorClearing()

        // CREATE PROFILE BUTTON
        binding.btnCreateProfile.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val gender = binding.etGender.text.toString().trim()
            val dob = selectedDobIso
            val email = binding.etEmail.text.toString().trim()

            clearAllErrors()

            when {
                firstName.isEmpty() -> {
                    binding.tilFirstName.error = "First name is required"
                    binding.etFirstName.requestFocus()
                }
                lastName.isEmpty() -> {
                    binding.tilLastName.error = "Last name is required"
                    binding.etLastName.requestFocus()
                }
                gender.isEmpty() -> {
                    binding.tilGender.error = "Please select your gender"
                }
                dob.isNullOrEmpty() -> {
                    binding.tilDob.error = "Please select your date of birth"
                }
                email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    binding.tilEmail.error = "Enter a valid email"
                    binding.etEmail.requestFocus()
                }
                else -> {
                    viewModel.createProfile(firstName, lastName, gender, dob, email)
                }
            }
        }

        observeState()
    }

    private fun clearAllErrors() {
        binding.tilFirstName.error = null
        binding.tilLastName.error = null
        binding.tilGender.error = null
        binding.tilDob.error = null
        binding.tilEmail.error = null
    }

    private fun setupGenderDropdown() {
        val genders = listOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown_text,
            R.id.tvDropdownText,
            genders
        )
        binding.etGender.setAdapter(adapter)
        binding.etGender.keyListener = null
        binding.etGender.setOnClickListener { binding.etGender.showDropDown() }
        binding.tilGender.setEndIconOnClickListener { binding.etGender.showDropDown() }
        binding.etGender.setOnItemClickListener { _, _, _, _ ->
            binding.tilGender.error = null
        }
    }

    private fun setupDobPicker() {
        binding.etDob.setOnClickListener { showDobPicker() }
        binding.tilDob.setEndIconOnClickListener { showDobPicker() }
    }

    private fun showDobPicker() {
        val today = Calendar.getInstance()
        val initial = Calendar.getInstance().apply {
            add(Calendar.YEAR, -18) // just a sensible starting point, not an age restriction
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val picked = Calendar.getInstance()
                picked.set(year, month, dayOfMonth, 0, 0, 0)

                binding.etDob.setText(displayDateFormat.format(picked.time))
                selectedDobIso = isoDateFormat.format(picked.time)
                binding.tilDob.error = null
            },
            initial.get(Calendar.YEAR),
            initial.get(Calendar.MONTH),
            initial.get(Calendar.DAY_OF_MONTH)
        )

        // Some device/app theme combos render the dialog's OK/Cancel buttons in white-on-white
        // (invisible). Force a visible color explicitly so they always show up.
        datePickerDialog.setOnShowListener {
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
                ?.setTextColor(android.graphics.Color.parseColor("#2962FF"))
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                ?.setTextColor(android.graphics.Color.parseColor("#2962FF"))
        }

        // Future date of birth doesn't make sense.
        datePickerDialog.datePicker.maxDate = today.timeInMillis
        datePickerDialog.show()
    }

    // First letter of First Name / Last Name should always be capital, rest stays as typed.
    private fun setupNameCapitalization() {
        capitalizeFirstLetter(binding.etFirstName)
        capitalizeFirstLetter(binding.etLastName)
    }

    private fun capitalizeFirstLetter(editText: TextInputEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s.isNullOrEmpty()) return

                val original = s.toString()
                val capitalized = original.replaceFirstChar { ch ->
                    if (ch.isLowerCase()) ch.titlecase(Locale.US) else ch.toString()
                }

                if (capitalized != original) {
                    isFormatting = true
                    val cursor = editText.selectionStart
                    editText.setText(capitalized)
                    editText.setSelection(cursor.coerceIn(0, capitalized.length))
                    isFormatting = false
                }
            }
        })
    }

    // Clears the inline validation line the moment the user starts fixing the field.
    private fun setupInlineErrorClearing() {
        clearErrorOnType(binding.tilFirstName, binding.etFirstName)
        clearErrorOnType(binding.tilLastName, binding.etLastName)
        clearErrorOnType(binding.tilEmail, binding.etEmail)
    }

    private fun clearErrorOnType(til: TextInputLayout, editText: TextInputEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                til.error = null
            }
        })
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.authState.collect {
                when (it) {
                    is AuthState.Success -> {
                        appPreference.setUserName("${binding.etFirstName.text} ${binding.etLastName.text}")
                        appPreference.setEmail(binding.etEmail.text.toString())
                        appPreference.setGender(binding.etGender.text.toString())
                        selectedDobIso?.let { dob -> appPreference.setDob(dob) }

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
