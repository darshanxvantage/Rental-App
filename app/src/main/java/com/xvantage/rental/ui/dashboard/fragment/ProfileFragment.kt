package com.xvantage.rental.ui.dashboard.fragment

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.xvantage.rental.databinding.FragmentProfileBinding
import com.xvantage.rental.ui.auth.AuthActivity
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.R
import android.widget.EditText
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.fragment.app.activityViewModels
import com.xvantage.rental.ui.auth.AuthViewModel
import com.xvantage.rental.ui.manageProperty.ManagePropertyActivity
import com.xvantage.rental.ui.settings.SettingsActivity
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.viewModels
import com.xvantage.rental.ui.tenant.TenantListActivity
import com.xvantage.rental.ui.dashboard.TenantListViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.xvantage.rental.ui.auth.fragment.sealed.AuthState



@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var appPreference: AppPreference
    private val viewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    // Support contact shown in the Help & Support quick action.
    // TODO: replace with your real support email / phone before release.
    private val supportEmail = "support@xvantage.app"
    private val supportPhone = "+91 90000 00000"

    // Pastel-on-bold color pairs used for the initials avatar background.
    // Picked deterministically per-user so the same name always gets the
    // same color (instead of a random color on every screen open).
    private val avatarColors = listOf(
        "#2962FF", "#16A34A", "#F59E0B", "#7C3AED", "#E11D48", "#0EA5E9"
    )

    companion object {

        const val IMAGE_PICK_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            FragmentProfileBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }



    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        appPreference =
            AppPreference(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.authState.collect { state ->

                when (state) {

                    is AuthState.Success -> {

                        Toast.makeText(
                            requireContext(),
                            state.message ?: "Success",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is AuthState.Error -> {

                        Toast.makeText(
                            requireContext(),
                            state.error,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {}
                }
            }
        }

        profileViewModel.loadDashboardData()

        viewLifecycleOwner.lifecycleScope.launch {

            profileViewModel.propertyCount.collect {

                binding.tvListedCount.text =
                    it.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {

            profileViewModel.tenantCount.collect {

                binding.tvRentedCount.text =
                    it.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {

            profileViewModel.revenueTotal.collect { total ->

                binding.tvRevenue.text =
                    formatRupees(total)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {

            profileViewModel.landlordTier.collect { tier ->

                val tierColor =
                    Color.parseColor(tier.colorHex)

                binding.tvTierName.text =
                    tier.title

                binding.tvTierHint.text =
                    tier.hint

                binding.pbTier.progress =
                    tier.progress

                binding.pbTier.progressTintList =
                    ColorStateList.valueOf(tierColor)

                binding.cardAvatar.strokeColor =
                    tierColor
            }
        }

        binding.cardProperties.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    ManagePropertyActivity::class.java
                )
            )
        }

        binding.cardTenants.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    TenantListActivity::class.java
                )
            )
        }

        binding.cardRevenue.setOnClickListener {

            Toast.makeText(
                requireContext(),
                "Detailed revenue breakdown coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        // USER DATA

        binding.tvUserName.text =
            appPreference.getUserName()

        binding.tvPhone.text =
            appPreference.getPhone()

        binding.tvEmail.text =
            appPreference.getEmail()

        bindAccountInfo()

        bindVerificationBadges()

        bindProfileCompleteness()

        bindAvatar()


        // CAMERA BUTTON

        binding.btnCamera.setOnClickListener {

            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            try {

                startActivityForResult(
                    intent,
                    IMAGE_PICK_CODE
                )

            } catch (e: Exception) {

                Toast.makeText(
                    context,
                    "Unable to Open Gallery",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        // EDIT PROFILE BUTTON

        binding.btnEditProfile.setOnClickListener {
            showEditProfileBottomSheet()
        }

        // HEADER SETTINGS SHORTCUT

//        binding.btnHeaderSettings.setOnClickListener {
//
//            startActivity(
//                Intent(
//                    requireContext(),
//                    SettingsActivity::class.java
//                )
//            )
//        }

        // QUICK ACTIONS

        binding.btnQuickSettings.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    SettingsActivity::class.java
                )
            )
        }

        binding.btnQuickSecurity.setOnClickListener {
            showSecurityDialog()
        }

        binding.btnQuickHelp.setOnClickListener {
            showHelpDialog()
        }

        binding.btnQuickShare.setOnClickListener {
            shareApp()
        }

        // LOGOUT BUTTON

        binding.btnLogout.setOnClickListener {

            appPreference.logoutUser()

            startActivity(
                Intent(
                    requireContext(),
                    AuthActivity::class.java
                )
            )

            requireActivity().finish()
        }
    }

    // ───────────────────────── AVATAR ─────────────────────────

    private fun bindAvatar() {

        try {

            val imagePath =
                appPreference.getProfileImage()

            if (!imagePath.isNullOrEmpty()) {

                val file =
                    File(imagePath)

                if (file.exists()) {

                    binding.imgProfile.setImageURI(
                        Uri.fromFile(file)
                    )

                    binding.imgProfile.visibility =
                        View.VISIBLE

                    binding.tvInitials.visibility =
                        View.GONE

                    return
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }

        // No photo yet — show an initials avatar instead of a generic icon.

        val name =
            appPreference.getUserName()?.trim().orEmpty()

        binding.tvInitials.text =
            initialsFor(name)

        binding.tvInitials.backgroundTintList =
            ColorStateList.valueOf(
                Color.parseColor(colorFor(name))
            )

        binding.tvInitials.visibility =
            View.VISIBLE

        binding.imgProfile.visibility =
            View.GONE
    }

    private fun initialsFor(name: String): String {

        if (name.isBlank()) return "?"

        val parts =
            name.split(" ").filter { it.isNotBlank() }

        return when {

            parts.size >= 2 ->
                "${parts[0][0]}${parts[1][0]}".uppercase()

            parts.isNotEmpty() ->
                parts[0].take(1).uppercase()

            else -> "?"
        }
    }

    private fun colorFor(name: String): String {

        if (name.isBlank()) return avatarColors[0]

        val index =
            kotlin.math.abs(name.hashCode()) % avatarColors.size

        return avatarColors[index]
    }

    // ───────────────────── VERIFICATION BADGES ─────────────────────

    private fun bindVerificationBadges() {

        // Phone is collected via OTP during sign-up/login, so any saved
        // phone number can be treated as verified.
        val phoneVerified =
            !appPreference.getPhone().isNullOrBlank()

        binding.ivPhoneVerified.visibility =
            if (phoneVerified) View.VISIBLE else View.GONE

        // Email has no OTP flow yet — only mark it once it looks like a
        // genuinely valid address, rather than claiming false verification.
        val email =
            appPreference.getEmail()

        val emailVerified =
            !email.isNullOrBlank() &&
                    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

        binding.ivEmailVerified.visibility =
            if (emailVerified) View.VISIBLE else View.GONE
    }

    // ───────────────────── PROFILE COMPLETENESS ─────────────────────

    private fun bindProfileCompleteness() {

        val fields = listOf(
            appPreference.getUserName() to "your name",
            appPreference.getEmail() to "your email",
            appPreference.getCity() to "your city",
            appPreference.getState() to "your state",
            appPreference.getAge() to "your age"
        )

        val photoFilled =
            !appPreference.getProfileImage().isNullOrEmpty() &&
                    File(appPreference.getProfileImage()!!).exists()

        val filledCount =
            fields.count { !it.first.isNullOrBlank() } + if (photoFilled) 1 else 0

        val totalCount =
            fields.size + 1

        val percent =
            (filledCount * 100) / totalCount

        val missingField =
            fields.firstOrNull { it.first.isNullOrBlank() }?.second
                ?: if (!photoFilled) "a profile photo" else null

        val hint = when {

            percent >= 100 ->
                "Your profile is complete"

            missingField != null ->
                "Add $missingField to reach 100%"

            else ->
                "Almost there"
        }

        binding.tvCompletionPercent.text =
            "$percent%"

        binding.pbCompletion.progress =
            percent

        binding.pbCompletion.progressTintList =
            ColorStateList.valueOf(
                Color.parseColor(if (percent >= 100) "#16A34A" else "#2962FF")
            )

        binding.tvCompletionHint.text =
            hint
    }

    // ───────────────────────── ACCOUNT INFO ─────────────────────────

    private fun bindAccountInfo() {

        binding.tvCity.text =
            appPreference.getCity()

        binding.tvState.text =
            appPreference.getState()

        binding.tvAge.text =
            appPreference.getAge()
    }

    // ───────────────────────── QUICK ACTIONS ─────────────────────────

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun showSecurityDialog() {

        val context = requireContext()

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(20), dp(24), dp(8))
        }

        val switchBiometric = Switch(context).apply {
            text = "Biometric Lock"
            textSize = 16f
            isChecked = appPreference.isBiometricEnabled()
        }

        val switchTwoStep = Switch(context).apply {
            text = "Two-Step Verification"
            textSize = 16f
            isChecked = appPreference.isTwoStepEnabled()

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(18)
            }
        }

        container.addView(switchBiometric)
        container.addView(switchTwoStep)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Security")
            .setView(container)
            .setCancelable(true)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {

            val btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val btnCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            btnSave.isAllCaps = false
            btnCancel.isAllCaps = false

            btnSave.setTextColor(Color.parseColor("#2962FF"))
            btnCancel.setTextColor(Color.parseColor("#FF3B30"))

            btnSave.setOnClickListener {

                appPreference.setBiometricEnabled(
                    switchBiometric.isChecked
                )

                appPreference.setTwoStepEnabled(
                    switchTwoStep.isChecked
                )

                Toast.makeText(
                    context,
                    "Security Updated Successfully",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showHelpDialog() {

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Help & Support")
            .setMessage(
                "Need help managing your properties or tenants?\n\n" +
                        "📧 Email : $supportEmail\n\n" +
                        "📞 Phone : $supportPhone"
            )
            .setCancelable(true)
            .setPositiveButton("Email Us", null)
            .setNegativeButton("Close", null)
            .create()

        dialog.setOnShowListener {

            val btnEmail =
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            val btnClose =
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            btnEmail.isAllCaps = false
            btnClose.isAllCaps = false

            btnEmail.setTextColor(Color.parseColor("#2962FF"))
            btnClose.setTextColor(Color.parseColor("#FF3B30"))

            btnEmail.setOnClickListener {

                try {

                    startActivity(

                        Intent(Intent.ACTION_SENDTO).apply {

                            data = Uri.parse("mailto:$supportEmail")

                            putExtra(
                                Intent.EXTRA_SUBJECT,
                                "Rental App Support"
                            )
                        }

                    )

                } catch (e: Exception) {

                    Toast.makeText(
                        requireContext(),
                        "No Email App Found",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                dialog.dismiss()
            }

            btnClose.setOnClickListener {

                dialog.dismiss()

            }
        }

        dialog.show()
    }

    private fun shareApp() {

        val shareText =
            "I'm managing my rental properties with this app — give it a try!"

        val intent =
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

        startActivity(
            Intent.createChooser(intent, "Share via")
        )
    }

    private fun formatRupees(amount: Double): String {

        return "₹" + "%,.0f".format(amount)
    }

    private fun showEditProfileBottomSheet() {

        val bottomSheet =
            com.google.android.material.bottomsheet.BottomSheetDialog(
                requireContext()
            )

        val view = layoutInflater.inflate(
            R.layout.bottomsheet_edit_profile,
            null
        )

        bottomSheet.setContentView(view)

        bottomSheet.behavior.state =
            BottomSheetBehavior.STATE_EXPANDED

        val etFirstName =
            view.findViewById<EditText>(R.id.etFirstName)

        val etLastName =
            view.findViewById<EditText>(R.id.etLastName)

        val etEmail =
            view.findViewById<EditText>(R.id.etEmail)

        val etState =
            view.findViewById<EditText>(R.id.etState)

        val etCity =
            view.findViewById<EditText>(R.id.etCity)

        val etAge =
            view.findViewById<EditText>(R.id.etAge)

        val btnSave =
            view.findViewById<MaterialButton>(R.id.btnSave)

        // AUTO FILL


        val fullName =
            appPreference.getUserName()?.trim() ?: ""

        val nameParts =
            fullName.split(" ")

        if (nameParts.isNotEmpty()) {

            etFirstName.setText(
                nameParts[0]
            )
        }

        if (nameParts.size > 1) {

            etLastName.setText(
                nameParts.drop(1).joinToString(" ")
            )
        }


        etEmail.setText(
            appPreference.getEmail()
        )

        etCity.setText(
            appPreference.getCity()
        )

        etAge.setText(
            appPreference.getAge()
        )
        etState.setText(
            appPreference.getState()
        )



        btnSave.setOnClickListener {

            val firstName =
                etFirstName.text.toString().trim()

            val lastName =
                etLastName.text.toString().trim()

            val email =
                etEmail.text.toString().trim()

            val state =
                etState.text.toString().trim()

            val city =
                etCity.text.toString().trim()

            val age =
                etAge.text.toString().trim()

            when {

                firstName.isEmpty() -> {
                    etFirstName.error = "Enter First Name"
                    etFirstName.requestFocus()
                }

                lastName.isEmpty() -> {
                    etLastName.error = "Enter Last Name"
                    etLastName.requestFocus()
                }

                email.isEmpty() -> {
                    etEmail.error = "Enter Email"
                    etEmail.requestFocus()
                }

                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etEmail.error = "Enter Valid Email"
                    etEmail.requestFocus()
                }

                state.isEmpty() -> {
                    etState.error = "Enter State"
                    etState.requestFocus()
                }

                city.isEmpty() -> {
                    etCity.error = "Enter City"
                    etCity.requestFocus()
                }

                age.toIntOrNull() == null -> {
                    etAge.error = "Enter Valid Age"
                    etAge.requestFocus()
                }

                age.toInt() < 18 -> {
                    etAge.error = "Age must be 18+"
                    etAge.requestFocus()
                }

                else -> {

                    // SAVE DATA

                    appPreference.setUserName(
                        "$firstName $lastName"
                    )

                    appPreference.setEmail(
                        email
                    )

                    appPreference.setState(
                        state
                    )

                    appPreference.setCity(
                        city
                    )

                    appPreference.setAge(
                        age
                    )

                    // REFRESH PROFILE SCREEN

                    binding.tvUserName.text =
                        "$firstName $lastName"

                    binding.tvEmail.text =
                        email

                    bindAccountInfo()

                    bindVerificationBadges()

                    bindProfileCompleteness()

                    bindAvatar()

                    Toast.makeText(
                        requireContext(),
                        "Profile Updated Successfully"
                        ,
                        Toast.LENGTH_SHORT
                    ).show()

                    bottomSheet.dismiss()
                }
            }
        }

        bottomSheet.show()
    }




    // IMAGE PICK RESULT

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == IMAGE_PICK_CODE &&
            resultCode == Activity.RESULT_OK &&
            data != null
        ) {

            val imageUri =
                data.data

            try {

                imageUri?.let {

                    val bitmap: Bitmap =

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                            val source =
                                ImageDecoder.createSource(
                                    requireActivity().contentResolver,
                                    it
                                )

                            ImageDecoder.decodeBitmap(source)

                        } else {

                            MediaStore.Images.Media.getBitmap(
                                requireActivity().contentResolver,
                                it
                            )
                        }

                    val file =
                        File(
                            requireContext().filesDir,
                            "profile_image.jpg"
                        )

                    val outputStream =
                        FileOutputStream(file)

                    val resizedBitmap = Bitmap.createScaledBitmap(
                        bitmap,
                        800,
                        800,
                        true
                    )

                    resizedBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        60,
                        outputStream
                    )

                    outputStream.flush()

                    outputStream.close()

                    binding.imgProfile.setImageBitmap(resizedBitmap)

                    binding.imgProfile.visibility =
                        View.VISIBLE

                    binding.tvInitials.visibility =
                        View.GONE

                    appPreference.setProfileImage(
                        file.absolutePath
                    )

                    bindProfileCompleteness()

                    viewModel.updateProfileImage(
                        appPreference.getUserName() ?: "User",
                        file
                    )
                }

            } catch (e: Exception) {

                e.printStackTrace()

                Toast.makeText(
                    context,
                    e.message ?: "Unknown Error",
                    Toast.LENGTH_LONG
                ).show()
            }


        }
    }
}

