package com.xvantage.rental.ui.addProperty.activity

import android.Manifest
import com.bumptech.glide.Glide
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.xvantage.rental.utils.AppSpinnerAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xvantage.rental.network.request.property.UpdatePropertyRequest
import com.xvantage.rental.BuildConfig
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityAddPropertyBinding
import com.xvantage.rental.network.request.property.CreatePropertyRequest
import com.xvantage.rental.ui.addProperty.AddPropertyViewModel
import com.xvantage.rental.ui.addProperty.CreatePropertyState
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.CommonFunction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


@AndroidEntryPoint
class AddPropertyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPropertyBinding
    private lateinit var appPreference: AppPreference
    private val viewModel: AddPropertyViewModel by viewModels()
    private var propertyTypeIds = listOf<String>()
    private var selectedPropertyTypeId: String = ""
    private lateinit var llPropertyImage: View

    private var propertyImage: Uri? = null
    private var imageFile: File? = null

    private var isEditMode = false

    private var propertyId = ""

    // Property type id + image URL fetched from the details API for edit
    // mode. propertyTypeIds isn't populated yet when this arrives (it loads
    // async), so we stash it here and apply it once the spinner is ready.
    private var pendingEditPropertyTypeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_property)
        appPreference = AppPreference(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding.toolbar.tvTitle.setText(R.string.add_property_bottom_n)

        llPropertyImage = findViewById(R.id.ll_property_photo)

        isEditMode =
            intent.getBooleanExtra(
                "isEdit",
                false
            )

        propertyId =
            intent.getStringExtra(
                "propertyId"
            ) ?: ""

        if (isEditMode) {

            binding.toolbar.tvTitle.text =
                "Edit Property"

            binding.etSignUpEmail.setText(
                intent.getStringExtra(
                    "propertyName"
                )
            )

            binding.etAddress.setText(
                intent.getStringExtra(
                    "propertyAddress"
                )
            )

            binding.etHomeNumber.setText(
                intent.getStringExtra(
                    "propertyRooms"
                )
            )

            binding.etOwnerName.setText(
                appPreference.getUserName()
            )

            binding.etWhatsappNumber.setText(
                intent.getStringExtra(
                    "waNumber"
                ) ?: ""
            )

            binding.toolbar.btnSave.text =
                "Update"
        }

        initViews()
        initClickEvents()
        observeViewModelStates()

        if (isEditMode) {
            observeEditPropertyDetails()
            viewModel.loadPropertyForEdit(propertyId)
        }
    }

    /**
     * Fetch the full property details (name, address, rooms, owner,
     * WhatsApp number, property TYPE and PHOTO) and pre-fill every field.
     * The Intent extras from Manage Property only cover a subset of these,
     * which is why Property Type and Photo used to show up empty.
     */
    private fun observeEditPropertyDetails() {

        lifecycleScope.launch {

            viewModel.editPropertyDetails.collect { details ->

                if (details == null) return@collect

                binding.etSignUpEmail.setText(details.name)

                binding.etAddress.setText(details.address)

                binding.etHomeNumber.setText(
                    details.totalRooms.toString()
                )

                binding.etOwnerName.setText(details.ownerName)

                binding.etWhatsappNumber.setText(details.waNumber)

                pendingEditPropertyTypeId = details.propertyTypeId

                trySelectPendingPropertyType()

                if (details.propertyImage.isNotBlank()) {

                    Glide.with(this@AddPropertyActivity)
                        .load(details.propertyImage)
                        .placeholder(R.drawable.image)
                        .error(R.drawable.image)
                        .into(binding.llPropertyPhoto.ivThumbnail)

                    binding.llPropertyPhoto.tvFileName.text =
                        "Current property photo"

                    binding.llPropertyPhoto.tvFileSize.text = ""

                    llPropertyImage.visibility = View.VISIBLE
                    binding.llAddPhoto.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Selects the correct item in the Property Type spinner once both the
     * property type list (loaded async from the API) and the property's
     * own propertyTypeId (loaded async from the edit details API) are
     * available. Safe to call multiple times / in either order.
     */
    private fun trySelectPendingPropertyType() {

        val typeId = pendingEditPropertyTypeId ?: return

        val index = propertyTypeIds.indexOf(typeId)

        if (index >= 0) {

            selectedPropertyTypeId = typeId

            binding.spinnerPropertyType.setSelection(index + 1)
        }
    }

    /**
     * Observe ViewModel states for data updates and API responses
     */
    private fun observeViewModelStates() {
        lifecycleScope.launch {
            viewModel.createPropertyState.collectLatest { state ->
                when (state) {
                    is CreatePropertyState.Loading -> {
                    }
                    is CreatePropertyState.Success -> {

                        val message =

                            if (isEditMode)
                                "Property updated successfully"
                            else
                                "Property created successfully"

                        Toast.makeText(
                            this@AddPropertyActivity,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()

                        if (!isEditMode) {

                            val currentCount =
                                appPreference.getListedCount()

                            appPreference.setListedCount(
                                currentCount + 1
                            )
                        }

                        val propertyId =
                            state.data.data.id

                        val intent =
                            Intent(
                                this@AddPropertyActivity,
                                PropertyDetailsActivity::class.java
                            ).apply {

                                putExtra(
                                    "propertyId",
                                    propertyId
                                )
                            }

                        startActivity(intent)

                        finish()
                    }
                    is CreatePropertyState.Error -> {
//                        Toast.makeText(this@AddPropertyActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> { /* Idle – no op */ }
                }
            }
        }
    }

    /**
     * Initialize click event listeners for UI components.
     */
    private fun initClickEvents() {
        binding.toolbar.back.setOnClickListener { onBackPressed() }

        binding.toolbar.btnSave.setOnClickListener {

            if (!validateInputs()) {
                return@setOnClickListener
            }

            if (isEditMode) {

                updateProperty()

            } else {

                submitProperty()
            }
        }

        binding.llAddPhoto.setOnClickListener { checkPermissionsAndOpenOptions() }

        binding.llPropertyPhoto.btnClose.setOnClickListener {
            propertyImage = null
            imageFile = null
            llPropertyImage.visibility = View.GONE
            binding.llAddPhoto.visibility = View.VISIBLE
        }
    }

    /**
     * Validate all required inputs before submission
     */
    private fun validateInputs(): Boolean {
        // Validate property type selection
//        if (selectedPropertyTypeId.isEmpty()) {
//            Toast.makeText(this, "Please select a property type", Toast.LENGTH_SHORT).show()
//            return false
//        }

        // Validate address
        if (binding.etAddress.text.toString().trim().isEmpty()) {
            binding.etAddress.error = "Please enter property address"
            binding.etAddress.requestFocus()
            return false
        }

        // Validate property name
        if (binding.etSignUpEmail.text.toString().trim().isEmpty()) {

            binding.etSignUpEmail.error =
                "Please enter property name"

            binding.etSignUpEmail.requestFocus()

            return false
        }

        // Validate WhatsApp number
        if (binding.etWhatsappNumber.text.toString().trim().isEmpty()) {
            binding.etWhatsappNumber.error = "Please enter WhatsApp number"
            binding.etWhatsappNumber.requestFocus()
            return false
        }

        return true
    }


    private suspend fun compressPropertyImage(uri: Uri?): Uri? =
        withContext(Dispatchers.IO) {

            if (uri == null) {
                return@withContext null
            }

            try {

                val inputStream =
                    contentResolver.openInputStream(uri)

                val bitmap =
                    BitmapFactory.decodeStream(inputStream)

                inputStream?.close()

                if (bitmap == null) {
                    return@withContext uri
                }

                val maxWidth = 1280
                val maxHeight = 1280

                val width = bitmap.width
                val height = bitmap.height

                val scale = minOf(
                    maxWidth.toFloat() / width,
                    maxHeight.toFloat() / height,
                    1f
                )

                val newWidth = (width * scale).toInt()
                val newHeight = (height * scale).toInt()

                val resizedBitmap =
                    if (newWidth != width || newHeight != height) {

                        Bitmap.createScaledBitmap(
                            bitmap,
                            newWidth,
                            newHeight,
                            true
                        )

                    } else {
                        bitmap
                    }

                val compressedFile = File(
                    cacheDir,
                    "property_compressed_${System.currentTimeMillis()}.jpg"
                )

                FileOutputStream(compressedFile).use { outputStream ->

                    resizedBitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        70,
                        outputStream
                    )
                }

                if (resizedBitmap !== bitmap) {
                    resizedBitmap.recycle()
                }

                bitmap.recycle()

                Uri.fromFile(compressedFile)

            } catch (e: Exception) {

                Log.e(
                    "AddProperty",
                    "Image compression failed",
                    e
                )

                uri
            }
        }

    /**
     * Create and submit property data
     */
    private fun submitProperty() {

        lifecycleScope.launch {

            binding.toolbar.btnSave.isEnabled = false

            try {


                val compressedImage =
                    compressPropertyImage(propertyImage)

                val request = CreatePropertyRequest(

                    address =
                        binding.etAddress.text
                            .toString()
                            .trim(),

                    noOfRoom =
                        binding.etHomeNumber.text
                            .toString()
                            .toIntOrNull() ?: 0,

                    propertyTypeId =
                        selectedPropertyTypeId,

                    wa_number =
                        binding.etWhatsappNumber.text
                            .toString()
                            .trim(),

                    name =
                        binding.etSignUpEmail.text
                            .toString()
                            .trim(),

                    imageUri = compressedImage
                )

                Log.d(
                    "AddProperty",
                    "Submitting with typeId: $selectedPropertyTypeId"
                )

                // 3. Send request
                viewModel.createProperty(request)

            } catch (e: Exception) {

                binding.toolbar.btnSave.isEnabled = true

                Log.e(
                    "AddProperty",
                    "Image compression failed",
                    e
                )

//                Toast.makeText(
//                    this@AddPropertyActivity,
//                    "Failed to process property image",
//                    Toast.LENGTH_SHORT
//                ).show()
            }
        }
    }
    private fun updateProperty() {

        lifecycleScope.launch {

            binding.toolbar.btnSave.isEnabled = false

            try {

                val compressedImage =
                    compressPropertyImage(propertyImage)

                val request =
                    UpdatePropertyRequest(

                        propertyId = propertyId,

                        address =
                            binding.etAddress.text
                                .toString()
                                .trim(),

                        noOfRoom =
                            binding.etHomeNumber.text
                                .toString()
                                .toIntOrNull() ?: 0,

                        propertyTypeId =
                            selectedPropertyTypeId,

                        wa_number =
                            binding.etWhatsappNumber.text
                                .toString()
                                .trim(),

                        name =
                            binding.etSignUpEmail.text
                                .toString()
                                .trim(),

                        imageUri =
                            compressedImage
                    )

                viewModel.updateProperty(
                    request
                )

            } catch (e: Exception) {

                binding.toolbar.btnSave.isEnabled = true

                Log.e(
                    "AddProperty",
                    "Update property image processing failed",
                    e
                )

//                Toast.makeText(
//                    this@AddPropertyActivity,
//                    "Failed to process property image",
//                    Toast.LENGTH_SHORT
//                ).show()
            }
        }
    }


    /**
     * Initialize all views and drop-down menus.
     */
    private fun initViews() {

        // First show loading text instead of blank spinner
        setupSpinner(
            binding.spinnerPropertyType,
            listOf("Loading property types...")
        )


        // Property Type selection listener
        binding.spinnerPropertyType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {

                    if (
                        pos > 0 &&
                        pos - 1 < propertyTypeIds.size
                    ) {

                        selectedPropertyTypeId =
                            propertyTypeIds[pos - 1]

                        Log.d(
                            "AddProperty",
                            "Selected typeId=$selectedPropertyTypeId"
                        )

                    } else {

                        selectedPropertyTypeId = ""
                    }

                    updatePropertySection(pos)
                }


                override fun onNothingSelected(
                    parent: AdapterView<*>
                ) {

                    selectedPropertyTypeId = ""
                }
            }


        // Observe Property Type API data
        lifecycleScope.launch {

            viewModel.propertyTypes.collect { list ->

                Log.d(
                    "PROPERTY_TYPE_UI",
                    "Received list size=${list.size}, data=$list"
                )

                if (list.isNotEmpty()) {

                    propertyTypeIds =
                        list.map { it.id }

                    val names =
                        list.map { it.name }

                    val spinnerItems =
                        listOf("Select Property Type") + names

                    setupSpinner(
                        binding.spinnerPropertyType,
                        spinnerItems
                    )

                    // In case the edit-details API already returned before
                    // this list finished loading.
                    trySelectPendingPropertyType()
                }
            }
        }


        // Observe Property Type API errors
        lifecycleScope.launch {

            viewModel.propertyTypeError.collect { error ->

                if (!error.isNullOrBlank()) {

                    Log.e(
                        "PROPERTY_TYPE_UI",
                        error
                    )

                    setupSpinner(
                        binding.spinnerPropertyType,
                        listOf("Unable to load property types")
                    )

//                    Toast.makeText(
//                        this@AddPropertyActivity,
//                        error,
//                        Toast.LENGTH_LONG
//                    ).show()
                }
            }
        }


        // API call AFTER observers are attached
        viewModel.loadPropertyTypes()
    }

    /**
     * Helper function to setup a spinner with custom text color.
     */
    private fun setupSpinner(
        spinner: Spinner,
        items: List<String>
    ) {

        val adapter = AppSpinnerAdapter(
            this@AddPropertyActivity,
            items
        )

        spinner.adapter = adapter

        spinner.dropDownVerticalOffset = 8
    }

    /**
     * Update the visibility of UI sections based on the selected property type.
     */
    private fun updatePropertySection(selectedPosition: Int) {
        binding.llHomeNumber.visibility = if (selectedPosition == 1) View.VISIBLE else View.GONE
    }

    /**
     * Checks required permissions and opens photo options dialog.
     */
    private fun checkPermissionsAndOpenOptions() {
        val requiredPermissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requiredPermissions.add(Manifest.permission.CAMERA)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requiredPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (requiredPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, requiredPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            showPhotoOptionsDialog()
        }
    }

    /**
     * Display a dialog to choose between camera and gallery.
     */
    private fun showPhotoOptionsDialog() {
        val options = arrayOf("Open Camera", "Choose from Gallery")
        MaterialAlertDialogBuilder(this)
            .setTitle("Add Photo")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    // ActivityResultLaunchers for Camera and Gallery
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                propertyImage?.let {
                    try {
                        // Make sure the image exists and is readable
                        contentResolver.openInputStream(it)?.close()
                        updatePhotoUI(it)
                    } catch (e: IOException) {
                        Log.e("AddProperty", "Camera image file not accessible", e)
//                        Toast.makeText(this, "Failed to process photo: ${e.message}", Toast.LENGTH_SHORT).show()
                        propertyImage = null
                    }
                }
                    ?: Toast.makeText(this, "Failed to capture photo!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Photo capture cancelled!", Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    // Create a local copy of the file from the content URI
                    val inputStream = contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        // Create a new file in app's cache directory
                        imageFile = File(cacheDir, "image_${System.currentTimeMillis()}.jpg")
                        imageFile?.outputStream()?.use { fileOut ->
                            inputStream.copyTo(fileOut)
                        }
                        inputStream.close()

                        // Use the local file URI instead of content URI
                        propertyImage = Uri.fromFile(imageFile)
                        updatePhotoUI(propertyImage!!)
                    } else {
                        Toast.makeText(this, "Cannot access selected image", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    Log.e("AddProperty", "Error copying gallery image", e)
                    Toast.makeText(this, "Failed to process selected image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }

    /**
     * Update the UI with the selected or captured photo.
     */
    private fun updatePhotoUI(photoUri: Uri) {
        try {
            binding.llPropertyPhoto.ivThumbnail.setImageURI(photoUri)
            val fileName = CommonFunction().getFileName(this, photoUri)
            binding.llPropertyPhoto.tvFileName.text = fileName ?: "photo.jpg"
            val fileSize = CommonFunction().getFileSize(this, photoUri)
            binding.llPropertyPhoto.tvFileSize.text = fileSize ?: "Unknown size"

            llPropertyImage.visibility = View.VISIBLE
            binding.llAddPhoto.visibility = View.GONE
        } catch (e: Exception) {
            Log.e("AddProperty", "Error updating photo UI", e)
//            Toast.makeText(this, "Error displaying image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens the device camera to capture a photo.
     */
    private fun openCamera() {
        try {
            val photoFile = File(
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "IMG_${System.currentTimeMillis()}.jpg"
            )
            imageFile = photoFile
            propertyImage = FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                photoFile
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, propertyImage)
            }
            if (intent.resolveActivity(packageManager) != null) {
                cameraLauncher.launch(intent)
            } else {
//                Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("AddProperty", "Error opening camera", e)
//            Toast.makeText(this, "Error opening camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens the gallery for image selection.
     */
    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showPhotoOptionsDialog()
            } else {
//                Toast.makeText(this, "Permissions are required to proceed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 101
    }
}