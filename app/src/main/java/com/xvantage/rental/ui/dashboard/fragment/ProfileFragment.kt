package com.xvantage.rental.ui.dashboard.fragment

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.widget.TextView
import android.net.Uri
import androidx.core.app.ShareCompat
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.app.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.Manifest
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.xvantage.rental.R
import com.xvantage.rental.databinding.FragmentProfileBinding
import com.xvantage.rental.ui.auth.AuthViewModel
import com.xvantage.rental.ui.auth.fragment.sealed.AuthState
import com.xvantage.rental.ui.manageProperty.ManagePropertyActivity
//import com.xvantage.rental.ui.settings.SettingsActivity
import com.xvantage.rental.ui.tenant.TenantListActivity
import com.xvantage.rental.utils.AppPreference
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var appPreference: AppPreference
    private val viewModel: AuthViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()

    private val supportEmail = "support@xvantage.app"
    private val supportPhone = "+91 98798 99654"

    private val avatarColors = listOf(
        "#2962FF", "#16A34A", "#F59E0B", "#7C3AED", "#E11D48", "#0EA5E9"
    )

    companion object {

        const val IMAGE_PICK_CODE = 1001

        const val CAMERA_REQUEST = 1002

    }

    override fun onResume() {
        super.onResume()
        profileViewModel.loadDashboardData()
        bindAvatar()
        bindProfileCompleteness()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appPreference = AppPreference(requireContext())

        // Observe auth state (logout/success messages)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Success -> {
                        Toast.makeText(requireContext(), state.message ?: "Success", Toast.LENGTH_SHORT).show()
                    }
                    is AuthState.Error -> {
                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_LONG).show()
                    }
                    else -> {}
                }
            }
        }

        // Load fresh data from API
        profileViewModel.loadDashboardData()

        // Properties count
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.propertyCount.collect { count ->
                binding.tvListedCount.text = count.toString()
            }
        }

        // Active tenant count
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.tenantCount.collect { count ->
                binding.tvRentedCount.text = count.toString()
            }
        }

        // Revenue = monthly rent total of ACTIVE tenants
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.revenueTotal.collect { total ->
                binding.tvRevenue.text = formatRupees(total)
            }
        }

        // Landlord tier badge
        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.landlordTier.collect { tier ->
                val tierColor = Color.parseColor(tier.colorHex)
                binding.tvTierName.text = tier.title
                binding.tvTierHint.text = tier.hint
                binding.pbTier.progress = tier.progress
                binding.pbTier.progressTintList = ColorStateList.valueOf(tierColor)
                binding.cardAvatar.strokeColor = Color.parseColor("#2ECC71")
            }
        }

        // Stats card click listeners
        binding.cardProperties.setOnClickListener {
            startActivity(Intent(requireContext(), ManagePropertyActivity::class.java))
        }

        binding.cardTenants.setOnClickListener {
            startActivity(Intent(requireContext(), TenantListActivity::class.java))
        }

        binding.cardRevenue.setOnClickListener {
            // Show revenue breakdown dialog with collected vs pending
            showRevenueDialog()
        }

        // Bind user data from preferences
        binding.tvUserName.text = appPreference.getUserName()
        binding.tvPhone.text = appPreference.getPhone()
        binding.tvEmail.text = appPreference.getEmail()

        bindVerificationBadges()
        bindProfileCompleteness()
        bindAvatar()

        // Camera button - pick photo from gallery
        binding.btnCamera.setOnClickListener {
            checkMediaPermissions()
        }

        // Edit icon (now placed next to the user's name)
        binding.btnQuickSettings.setOnClickListener {
            showEditProfileBottomSheet()
        }
    }

    private fun checkMediaPermissions() {

        val permissions = mutableListOf<String>()

        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            }

        } else {

            if (
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

        }

        if (permissions.isEmpty()) {

            showImagePickerDialog()

        } else {

            ActivityCompat.requestPermissions(
                requireActivity(),
                permissions.toTypedArray(),
                500
            )

        }
    }

    private fun showImagePickerDialog() {

        val options = arrayOf(
            "Open Camera",
            "Choose from Gallery"
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Photo")
            .setItems(options) { _, which ->

                when (which) {

                    0 -> openCamera()

                    1 -> openGallery()

                }

            }
            .show()
    }

    private fun openGallery() {

        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun openCamera() {

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        startActivityForResult(intent, CAMERA_REQUEST)

    }

    private fun showRevenueDialog() {
        viewLifecycleOwner.lifecycleScope.launch {

            val monthly = profileViewModel.monthlyRentTotal.value

            val collected = profileViewModel.collectedTotal.value

            val pending = profileViewModel.pendingTotal.value

            val rate = profileViewModel.collectionRate.value

            val lifetime = profileViewModel.lifetimeRevenue.value


            val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(
                requireContext(),
                R.style.BottomSheetDialogTheme
            )
            val view = layoutInflater.inflate(R.layout.bottomsheet_revenue, null)
            dialog.setContentView(view)
            dialog.behavior.state =
                com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED


            val monthName = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault())
                .format(java.util.Date())
            view.findViewById<android.widget.TextView>(R.id.tvMonthBadge)?.text = monthName


            view.findViewById<android.widget.TextView>(R.id.tvMonthlyRent)
                ?.text = formatRupees(monthly)
            view.findViewById<android.widget.TextView>(R.id.tvCollected)
                ?.text = formatRupees(collected)
            view.findViewById<android.widget.TextView>(R.id.tvPending)
                ?.text = formatRupees(pending)
            view.findViewById<android.widget.TextView>(R.id.tvCollectionRate)
                ?.text = "$rate%"
            view.findViewById<TextView>(R.id.tvLifetimeRevenue)
                ?.text = formatRupees(lifetime)


            val pb = view.findViewById<android.widget.ProgressBar>(R.id.pbCollection)
            pb?.max = 100
            pb?.progress = rate


            view.findViewById<android.widget.TextView>(R.id.tvProgressPercent)
                ?.text = "$rate%"
            view.findViewById<android.widget.TextView>(R.id.tvCollectedLabel)
                ?.text = "${formatRupees(collected)} collected"
            view.findViewById<android.widget.TextView>(R.id.tvRemainingLabel)
                ?.text = "${formatRupees(pending)} remaining"


            view.findViewById<com.google.android.material.button.MaterialButton>(
                R.id.btnCloseRevenue
            )?.setOnClickListener { dialog.dismiss() }

            view.findViewById<com.google.android.material.button.MaterialButton>(
                R.id.btnViewDues
            )?.setOnClickListener {
                dialog.dismiss()
                requireActivity()
                    .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                        R.id.bottom_navigation
                    )?.selectedItemId = R.id.settings
            }

            dialog.show()
        }
    }



    private fun bindAvatar() {

        try {
            val imagePath = appPreference.getProfileImage()

            if (!imagePath.isNullOrEmpty()) {
                val file = File(imagePath)
                if (file.exists()) {
                    binding.imgProfile.setImageURI(Uri.fromFile(file))
                    binding.imgProfile.visibility = View.VISIBLE
                    binding.tvInitials.visibility = View.GONE
                    return
                }
            }

            val remoteUrl = appPreference.getRemoteProfileImageUrl()
            if (!remoteUrl.isNullOrEmpty()) {
                binding.imgProfile.visibility = View.VISIBLE
                binding.tvInitials.visibility = View.GONE

                com.bumptech.glide.Glide.with(this)
                    .load(remoteUrl)
                    .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                        override fun onLoadFailed(
                            e: com.bumptech.glide.load.engine.GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {

                            showInitialsAvatar()
                            return false
                        }

                        override fun onResourceReady(
                            resource: android.graphics.drawable.Drawable,
                            model: Any,
                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                            dataSource: com.bumptech.glide.load.DataSource,
                            isFirstResource: Boolean
                        ): Boolean = false
                    })
                    .into(binding.imgProfile)
                return
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        showInitialsAvatar()
    }

    private fun showInitialsAvatar() {
        val name = appPreference.getUserName()?.trim().orEmpty()
        binding.tvInitials.text = initialsFor(name)
        binding.tvInitials.backgroundTintList = ColorStateList.valueOf(
            Color.parseColor(colorFor(name))
        )
        binding.tvInitials.visibility = View.VISIBLE
        binding.imgProfile.visibility = View.GONE
    }

    private fun initialsFor(name: String): String {
        if (name.isBlank()) return "?"
        val parts = name.split(" ").filter { it.isNotBlank() }
        return when {
            parts.size >= 2 -> "${parts[0][0]}${parts[1][0]}".uppercase()
            parts.isNotEmpty() -> parts[0].take(1).uppercase()
            else -> "?"
        }
    }

    private fun colorFor(name: String): String {
        if (name.isBlank()) return avatarColors[0]
        return avatarColors[kotlin.math.abs(name.hashCode()) % avatarColors.size]
    }

    // ───────────────────── VERIFICATION BADGES ─────────────────────

    private fun bindVerificationBadges() {

        val phoneVerified = !appPreference.getPhone().isNullOrBlank()
        binding.ivPhoneVerified.visibility = if (phoneVerified) View.VISIBLE else View.GONE

        val email = appPreference.getEmail()
        val emailVerified = !email.isNullOrBlank() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        binding.ivEmailVerified.visibility = if (emailVerified) View.VISIBLE else View.GONE
    }

    // ───────────────────── PROFILE COMPLETENESS ─────────────────────

    private fun bindProfileCompleteness() {

        val fields = listOf(

            appPreference.getUserName() to "your name",

            appPreference.getEmail() to "your email",

            appPreference.getGender() to "your gender",

            appPreference.getDob() to "your date of birth"

        )

        val localImagePath = appPreference.getProfileImage()
        val hasLocalPhoto = !localImagePath.isNullOrEmpty() && File(localImagePath).exists()
        val hasRemotePhoto = !appPreference.getRemoteProfileImageUrl().isNullOrEmpty()
        val photoFilled = hasLocalPhoto || hasRemotePhoto

        val filledCount = fields.count { !it.first.isNullOrBlank() } + if (photoFilled) 1 else 0
        val totalCount = fields.size + 1
        val percent = (filledCount * 100) / totalCount

        val missingField = fields.firstOrNull { it.first.isNullOrBlank() }?.second
            ?: if (!photoFilled) "a profile photo" else null

        val hint = when {
            percent >= 100 -> "Your profile is complete"
            missingField != null -> "Add $missingField to reach 100%"
            else -> "Almost there"
        }

        val strengthColor = if (percent >= 100) "#16A34A" else "#E8892B"

        binding.tvCompletionPercent.text = "$percent%"
        binding.tvCompletionPercent.setTextColor(Color.parseColor(strengthColor))
        binding.pbCompletion.progress = percent
        binding.pbCompletion.progressTintList = ColorStateList.valueOf(
            Color.parseColor(strengthColor)
        )
        binding.tvCompletionHint.text = hint
    }

    // ───────────────────────── ACCOUNT INFO ─────────────────────────


    // ───────────────────────── QUICK ACTIONS ─────────────────────────

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

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

            val btnEmail = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val btnClose = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

            btnEmail.isAllCaps = false
            btnClose.isAllCaps = false
            btnEmail.setTextColor(Color.parseColor("#2962FF"))
            btnClose.setTextColor(Color.parseColor("#FF3B30"))

            btnEmail.setOnClickListener {
                try {
                    startActivity(Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$supportEmail")
                        putExtra(Intent.EXTRA_SUBJECT, "Rental App Support")
                    })
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No Email App Found", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }

            btnClose.setOnClickListener { dialog.dismiss() }
        }

        dialog.show()
    }

    private fun shareApp() {
        val playStoreLink = "https://play.google.com/store/apps/details?id=com.xv.rentalmaster&hl=en_IN"
        val shareText = """
🏠 xVantage Rental Master

Managing rental properties has never been easier!
✅ Track tenants & payments
✅ Due payment reminders  
✅ Property management at your fingertips

📲 Download now:
$playStoreLink
    """.trimIndent()

        // ✅ ShareCompat — app name share sheet mein dikhega
        ShareCompat.IntentBuilder(requireActivity())
            .setType("text/plain")
            .setSubject("xVantage Rental Master App")
            .setText(shareText)
            .setChooserTitle("Share xVantage Rental Master")
            .startChooser()
    }

    private fun formatRupees(amount: Double): String {
        return if (amount >= 100000) {
            "₹" + "%.1f".format(amount / 100000) + "L"
        } else if (amount >= 1000) {
            "₹" + "%,.0f".format(amount)
        } else {
            "₹" + amount.toInt().toString()
        }
    }

    private fun showEditProfileBottomSheet() {

        val bottomSheet = BottomSheetDialog(
            requireContext(),
            R.style.BottomSheetDialogTheme
        )

        val view = layoutInflater.inflate(
            R.layout.bottomsheet_edit_profile,
            null
        )

        bottomSheet.setContentView(view)

        bottomSheet.setOnShowListener {

            val bottomSheetInternal =
                bottomSheet.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                )

            bottomSheetInternal?.let {

                val behavior = BottomSheetBehavior.from(it)

                behavior.state = BottomSheetBehavior.STATE_EXPANDED

                behavior.skipCollapsed = true

                behavior.isFitToContents = true

                it.layoutParams.height =
                    ViewGroup.LayoutParams.WRAP_CONTENT

            }

        }

        val etFirstName = view.findViewById<EditText>(R.id.etFirstName)
        val etLastName  = view.findViewById<EditText>(R.id.etLastName)
        val etEmail     = view.findViewById<EditText>(R.id.etEmail)
        val etDob =
            view.findViewById<EditText>(R.id.etDob)
        val displayFormat =
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val apiFormat =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        var selectedDob = appPreference.getDob()
        val tilGender = view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilGender)
        val etGender = view.findViewById<android.widget.AutoCompleteTextView>(R.id.etGender)
        val genders = listOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown_text,
            R.id.tvDropdownText,
            genders
        )
        etGender.setAdapter(genderAdapter)
        etGender.setOnClickListener { etGender.showDropDown() }
        tilGender.setEndIconOnClickListener { etGender.showDropDown() }
        val btnSave     = view.findViewById<MaterialButton>(R.id.btnSave)

        // Auto-fill existing data
        val fullName = appPreference.getUserName()?.trim() ?: ""
        val nameParts = fullName.split(" ")
        if (nameParts.isNotEmpty()) etFirstName.setText(nameParts[0])
        if (nameParts.size > 1) etLastName.setText(nameParts.drop(1).joinToString(" "))

        etEmail.setText(appPreference.getEmail())

        etGender.setText(appPreference.getGender(), false)

        selectedDob?.let {

            try {

                val date = apiFormat.parse(it)

                if (date != null) {

                    etDob.setText(
                        displayFormat.format(date)
                    )

                }

            } catch (_: Exception) {
            }

        }

        etDob.setOnClickListener {

            val calendar = Calendar.getInstance()

            val picker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->

                    val selected = Calendar.getInstance()

                    selected.set(year, month, day)

                    etDob.setText(
                        displayFormat.format(selected.time)
                    )

                    selectedDob =
                        apiFormat.format(selected.time)

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            picker.datePicker.maxDate = System.currentTimeMillis()

            picker.show()
        }


        btnSave.setOnClickListener {

            val firstName = etFirstName.text.toString().trim()
            val lastName  = etLastName.text.toString().trim()
            val email     = etEmail.text.toString().trim()
            val gender    = etGender.text.toString().trim()
            val dob = selectedDob

            when {
                firstName.isEmpty() -> {
                    etFirstName.error = "⚠ Enter First Name"
                    etFirstName.requestFocus()
                }
                lastName.isEmpty() -> {
                    etLastName.error = "⚠ Enter Last Name"
                    etLastName.requestFocus()
                }
                email.isEmpty() -> {
                    etEmail.error = "⚠ Enter Email"
                    etEmail.requestFocus()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etEmail.error = "⚠ Enter Valid Email"
                    etEmail.requestFocus()
                }
                dob.isNullOrEmpty() -> {

                    Toast.makeText(
                        requireContext(),
                        "Please select Date of Birth",
                        Toast.LENGTH_SHORT
                    ).show()

                }
                gender.isEmpty() -> {
                    etGender.error = "⚠ Select Gender"
                    etGender.requestFocus()
                }

                else -> {

                    appPreference.setUserName("$firstName $lastName")
                    appPreference.setEmail(email)
                    appPreference.setGender(gender)
                    dob?.let {

                        appPreference.setDob(it)

                    }

                    binding.tvUserName.text = "$firstName $lastName"
                    binding.tvEmail.text = email

                    btnSave.isEnabled = false
                    viewModel.updateProfileImage(
                        firstName = firstName,
                        lastName = lastName
                    )
                }
            }
        }

        bottomSheet.show()
        viewModel.resetAuthState()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is com.xvantage.rental.ui.auth.fragment.sealed.AuthState.Success -> {

                        bindVerificationBadges()
                        bindProfileCompleteness()
                        bindAvatar()

                        Toast.makeText(requireContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show()

                        if (bottomSheet.isShowing) bottomSheet.dismiss()

                        viewModel.resetAuthState()
                    }

                    is com.xvantage.rental.ui.auth.fragment.sealed.AuthState.Error -> {

                        btnSave.isEnabled = true

                        Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()

                        viewModel.resetAuthState()
                    }

                    else -> Unit
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == 500) {

            if (
                grantResults.isNotEmpty() &&
                grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            ) {

                showImagePickerDialog()

            } else {

                Toast.makeText(
                    requireContext(),
                    "Permission denied",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }
    }


    // ───────────────────── IMAGE PICK RESULT ─────────────────────

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {

            val imageUri = data.data

            try {
                imageUri?.let {
                    val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(requireActivity().contentResolver, it)
                        )
                    } else {
                        MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
                    }

                    val file = File(requireContext().filesDir, "profile_image.jpg")
                    val outputStream = FileOutputStream(file)
                    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true)
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                    outputStream.flush()
                    outputStream.close()

                    binding.imgProfile.setImageBitmap(resizedBitmap)
                    binding.imgProfile.visibility = View.VISIBLE
                    binding.tvInitials.visibility = View.GONE

                    appPreference.setProfileImage(file.absolutePath)
                    bindProfileCompleteness()

                    viewModel.updateProfileImage(
                        imageFile = file
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
//                Toast.makeText(context, e.message ?: "Unknown Error", Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK && data != null) {

            val bitmap = data.extras?.get("data") as? Bitmap ?: return

            val file = File(requireContext().filesDir, "profile_image.jpg")

            val outputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)

            outputStream.flush()
            outputStream.close()

            binding.imgProfile.setImageBitmap(bitmap)
            binding.imgProfile.visibility = View.VISIBLE
            binding.tvInitials.visibility = View.GONE

            appPreference.setProfileImage(file.absolutePath)

            bindProfileCompleteness()

            viewModel.updateProfileImage(
                imageFile = file
            )
        }
    }


}