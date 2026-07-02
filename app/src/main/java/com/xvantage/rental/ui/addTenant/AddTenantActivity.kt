package com.xvantage.rental.ui.addTenant

import android.Manifest
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
import android.widget.ArrayAdapter
import com.xvantage.rental.network.response.PropertyItem
import com.xvantage.rental.network.response.PropertyRoom
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.xvantage.rental.utils.ImageCompressor
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xvantage.rental.BuildConfig
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityAddTenantBinding
import com.xvantage.rental.network.request.tenant.UpdateTenantRequest
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.CommonFunction
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Button
import android.widget.TextView
import java.io.File
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
/**
 * Activity to add a tenant.
 *
 * This activity uses static data and performs all UI logic for tenant data and image management.
 * In future iterations, a [TenantViewModel] will be integrated to supply dynamic data using MVVM.
 */
@AndroidEntryPoint
class AddTenantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTenantBinding
    private lateinit var appPreference: AppPreference

    private val viewModel by viewModels<AddTenantViewModel>()

    private var tenantId = ""

    private var isEditMode = false

    private var propertyList = ArrayList<PropertyItem>()

    private var selectedProperty: PropertyItem? = null

    private var selectedRoom: PropertyRoom? = null


    // Future integration: Use TenantViewModel to manage tenant data dynamically
//    private val tenantViewModel: TenantViewModel by viewModels()

    // Image URIs for various photos
    private var tenantImageUri: Uri? = null
    private var frontAdharImageUri: Uri? = null
    private var backAdharImageUri: Uri? = null

    // Views for displaying selected images
    private lateinit var llTenantPhoto: View
    private lateinit var llFrontAdharPhoto: View
    private lateinit var llBackAdharPhoto: View

    private val PERMISSION_REQUEST_CODE = 101
    private var selectedPicker = 1

    // Flags for expandable sections
    private var tenantDetailExpanded = true
    private var rentDetailExpanded = false
    private var waterBillDetailExpanded = false

    // Spinner options for electricity and water charges
    private val spinnerElecAndWaterOptions = arrayOf("No cost", "Fixed", "Metered")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_tenant)
        appPreference = AppPreference(this)
        tenantId =
            intent.getStringExtra(
                "tenantId"
            ) ?: ""

        isEditMode =
            tenantId.isNotEmpty()


        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set toolbar title
        if (isEditMode) {

            binding.toolbar.tvTitle.text =
                "Edit Tenant"

            binding.toolbar.btnSave.text =
                "Update"

        } else {

            binding.toolbar.tvTitle.setText(
                R.string.add_tenant_property
            )
        }

        // Initialize views for selected images
        llTenantPhoto = findViewById(R.id.ll_selected_tenant_photo)
        llFrontAdharPhoto = findViewById(R.id.ll_selected_front_adhar_photo)
        llBackAdharPhoto = findViewById(R.id.ll_selected_back_adhar_photo)

        // Setup click events and UI interactions
        setupClickEvents()
        setupSpinners()

        setupPropertySelection()
        viewModel.loadPropertyList()


        if (isEditMode) {

            viewModel.loadTenantDetails(
                tenantId
            )
        }

        lifecycleScope.launch {

            viewModel.tenantDetails.collect { response ->

                response?.let {

                    val tenant = it.data

                    binding.etTenantName.setText(
                        tenant.tenant_name ?: ""
                    )

                    binding.etPhoneNumber.setText(
                        tenant.phone_number ?: ""
                    )

                    binding.etReferenceName.setText(
                        tenant.refrence_name ?: ""
                    )

                    binding.llRentFinanceDetail.etRentAmount.setText(
                        tenant.rent ?: ""
                    )

                    binding.llRentFinanceDetail.etDepositAmount.setText(
                        tenant.room_deposit ?: ""
                    )

                    binding.llRentFinanceDetail.etRentAmount.setText(
                        tenant.rent ?: ""
                    )

                    binding.llRentFinanceDetail.etDepositAmount.setText(
                        tenant.room_deposit ?: ""
                    )
                    binding.llRentFinanceDetail.tvRentStartDate.text =
                        tenant.rent_start_date ?: ""

                    binding.llRentFinanceDetail.tvMoveInDate.text =
                        tenant.checkin_date ?: ""

                    binding.llElectricityFinanceDetail.etElectricityDefaultAmount.setText(
                        tenant.fixed_electricity_amount ?: ""
                    )

                    binding.llWaterFinanceDetail.etWaterFixedAmount.setText(
                        tenant.fixed_waterbill_amount ?: ""
                    )

                    binding.llWaterFinanceDetail.etWaterCostUnit.setText(
                        tenant.cost_unit_water ?: ""
                    )
                    binding.llElectricityFinanceDetail.etElectricityCostUnit.setText(
                        tenant.cost_per_unit ?: ""
                    )
                }
            }
        }

        lifecycleScope.launch {

            viewModel.propertyListState.collect {

                propertyList.clear()

                propertyList.addAll(it)

                val propertyNames =
                    propertyList.map { item ->

                        item.name

                    }

                val adapter = ArrayAdapter(

                    this@AddTenantActivity,

                    android.R.layout.simple_dropdown_item_1line,

                    propertyNames

                )

                binding.actProperty.setAdapter(adapter)

                if (isEditMode) {

                    val tenant = viewModel.tenantDetails.value?.data

                    tenant?.let {

                        val property = propertyList.firstOrNull { p ->
                            p.id == it.property_fk
                        }

                        property?.let { selected ->

                            selectedProperty = selected

                            binding.actProperty.setText(
                                selected.name,
                                false
                            )

                            loadRooms()

                        }

                    }

                }

            }

        }

        lifecycleScope.launch {

            viewModel.updateTenantState.collect { success ->

                if (success) {

                    Toast.makeText(
                        this@AddTenantActivity,
                        "Tenant updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    setResult(RESULT_OK)

                    finish()

                }

            }

        }
        lifecycleScope.launch {

            viewModel.createTenantState.collect { success ->

                if (success) {

                    Toast.makeText(
                        this@AddTenantActivity,
                        "Tenant created successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }







    /**
     * Sets up click event listeners for the activity.
     */
    private fun setupClickEvents() {
        // Photo selection click listeners
        binding.llAddTenantPhoto.setOnClickListener {
            selectedPicker = 1
            checkPermissionsAndOpenOptions()
        }
        binding.llAddFrontAdhar.setOnClickListener {
            selectedPicker = 2
            checkPermissionsAndOpenOptions()
        }
        binding.llAddBackAdhar.setOnClickListener {
            selectedPicker = 3
            checkPermissionsAndOpenOptions()
        }

        // Toolbar back click listener
        binding.toolbar.back.setOnClickListener { onBackPressed() }

        // Photo removal listeners
        binding.llSelectedTenantPhoto.btnClose.setOnClickListener {
            tenantImageUri = null
            llTenantPhoto.visibility = View.GONE
            binding.llAddTenantPhoto.visibility = View.VISIBLE
        }
        binding.llSelectedFrontAdharPhoto.btnClose.setOnClickListener {
            frontAdharImageUri = null
            llFrontAdharPhoto.visibility = View.GONE
            binding.llAddFrontAdhar.visibility = View.VISIBLE
        }
        binding.llSelectedBackAdharPhoto.btnClose.setOnClickListener {
            backAdharImageUri = null
            llBackAdharPhoto.visibility = View.GONE
            binding.llAddBackAdhar.visibility = View.VISIBLE
        }

        // Date picker click listeners for rent finance details
        binding.llRentFinanceDetail.tvMoveInDate.setOnClickListener {
            CommonFunction().showDatePickerDialog(this) { selectedDate ->
                binding.llRentFinanceDetail.tvMoveInDate.text = selectedDate
            }
        }
        binding.llRentFinanceDetail.tvRentStartDate.setOnClickListener {
            CommonFunction().showDatePickerDialog(this) { selectedDate ->
                binding.llRentFinanceDetail.tvRentStartDate.text = selectedDate
            }
        }
        binding.llRentFinanceDetail.tvRentDueDate.setOnClickListener {
            CommonFunction().showDatePickerDialog(this) { selectedDate ->
                binding.llRentFinanceDetail.tvRentDueDate.text = selectedDate
            }
        }
        binding.llRentFinanceDetail.tvAggreementStartDate.setOnClickListener {
            CommonFunction().showDatePickerDialog(this) { selectedDate ->
                binding.llRentFinanceDetail.tvAggreementStartDate.text = selectedDate
            }
        }
        binding.llRentFinanceDetail.tvAggreementEndDate.setOnClickListener {
            CommonFunction().showDatePickerDialog(this) { selectedDate ->
                binding.llRentFinanceDetail.tvAggreementEndDate.text = selectedDate
            }
        }



        // YEH ADD KARO — toolbar save button ke liye
        binding.toolbar.btnSave.setOnClickListener {

            if (isEditMode) {

                updateTenant()

            } else {

                createTenant()

            }

        }

        binding.llRentFinanceDetail.rgLeaseType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_until_leave -> {
                    binding.llRentFinanceDetail.llLeaseDateSelection.visibility = View.GONE
                    Toast.makeText(this, "Until Leave selected", Toast.LENGTH_SHORT).show()
                }
                R.id.rb_fixed_define -> {
                    binding.llRentFinanceDetail.llLeaseDateSelection.visibility = View.VISIBLE
                }
            }
        }

        binding.tvTitleTenantDetail.setOnClickListener {
            tenantDetailExpanded = toggleSection(binding.llTenantDetail, binding.tvTitleTenantDetail, tenantDetailExpanded)
        }
        binding.tvTitleRentDetail.setOnClickListener {
            rentDetailExpanded = toggleSection(binding.llRentDetail, binding.tvTitleRentDetail, rentDetailExpanded)
        }
        binding.tvTitleElectWaterDetail.setOnClickListener {
            waterBillDetailExpanded = toggleSection(binding.llEleWaterDetail, binding.tvTitleElectWaterDetail, waterBillDetailExpanded)
        }
    }

    private fun setupPropertySelection() {

        binding.actProperty.setOnItemClickListener { _, _, position, _ ->

            selectedProperty = propertyList[position]

            loadRooms()

        }

    }
    private fun loadRooms() {

        val rooms = selectedProperty?.property_room_no ?: return

        if (rooms.isEmpty()) {

            binding.actRoom.setText("")
            binding.actRoom.setAdapter(null)

            Toast.makeText(
                this,
                "No rooms found in this property",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        // Room list banavo
        val roomList = ArrayList<String>()

        rooms.forEach { room ->

            val status = if (
                room.status.equals("VACANT", true)
            ) {
                "🟠 Vacant"
            } else {
                "🟢 Occupied"
            }

            roomList.add(
                "Room ${room.room_no}   $status"
            )
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            roomList
        )

        binding.actRoom.setAdapter(adapter)

        if (isEditMode) {

            val tenant = viewModel.tenantDetails.value?.data

            tenant?.let {

                val room = rooms.firstOrNull {

                    it.room_no ==
                            tenant.tenant_details?.room_no

                }
                room?.let {

                    selectedRoom = it

                    binding.actRoom.setText(
                        "Room ${it.room_no}",
                        false
                    )

                }

            }

        }

        binding.actRoom.setOnItemClickListener { _, _, position, _ ->

            selectedRoom = rooms[position]

            if (
                selectedRoom?.status.equals("OCCUPIED", true)
                ||
                selectedRoom?.status.equals("OCCUPED", true)
            ) {

                showOccupiedRoomDialog(
                    selectedRoom?.room_no ?: ""
                )

            } else {

                Toast.makeText(
                    this,
                    "Selected Room ${selectedRoom?.room_no}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Helper method to toggle section visibility and update the drawable.
     */
    private fun toggleSection(section: View, titleView: View, expanded: Boolean): Boolean {
        return if (expanded) {
            section.visibility = View.GONE
            (titleView as? android.widget.TextView)?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_drop_down_arrow, 0)
            false
        } else {
            section.visibility = View.VISIBLE
            (titleView as? android.widget.TextView)?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_up_arrow, 0)
            true
        }
    }

    /**
     * Sets up the spinners for electricity and water finance details.
     */
    private fun setupSpinners() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerElecAndWaterOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
        binding.llElectricityFinanceDetail.spElectricity.adapter = adapter
        binding.llWaterFinanceDetail.spWater.adapter = adapter

        binding.llElectricityFinanceDetail.spElectricity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        binding.llElectricityFinanceDetail.llDefaultElectricityDetails.visibility = View.GONE
                        binding.llElectricityFinanceDetail.llElectricityMeterDetails.visibility = View.GONE
                    }
                    1 -> {
                        binding.llElectricityFinanceDetail.llDefaultElectricityDetails.visibility = View.VISIBLE
                        binding.llElectricityFinanceDetail.llElectricityMeterDetails.visibility = View.GONE
                    }
                    2 -> {
                        binding.llElectricityFinanceDetail.llElectricityMeterDetails.visibility = View.VISIBLE
                        binding.llElectricityFinanceDetail.llDefaultElectricityDetails.visibility = View.GONE
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.llWaterFinanceDetail.spWater.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        binding.llWaterFinanceDetail.llDefaultWaterDetails.visibility = View.GONE
                        binding.llWaterFinanceDetail.llWaterMeterDetails.visibility = View.GONE
                    }
                    1 -> {
                        binding.llWaterFinanceDetail.llDefaultWaterDetails.visibility = View.VISIBLE
                        binding.llWaterFinanceDetail.llWaterMeterDetails.visibility = View.GONE
                    }
                    2 -> {
                        binding.llWaterFinanceDetail.llWaterMeterDetails.visibility = View.VISIBLE
                        binding.llWaterFinanceDetail.llDefaultWaterDetails.visibility = View.GONE
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * Displays a dialog to choose between capturing a photo via camera or selecting one from the gallery.
     */
    private fun showOptionsDialog() {
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

    /**
     * Opens the camera to capture an image.
     */
    private fun openCamera() {
        val photoFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMG_${System.currentTimeMillis()}.jpg")
        var intent: Intent? = null

        when (selectedPicker) {
            1 -> {
                tenantImageUri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", photoFile)
                intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, tenantImageUri)
                }
            }
            2 -> {
                frontAdharImageUri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", photoFile)
                intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, frontAdharImageUri)
                }
            }
            3 -> {
                backAdharImageUri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", photoFile)
                intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, backAdharImageUri)
                }
            }
        }

        if (intent?.resolveActivity(packageManager) != null) {
            cameraLauncher.launch(intent)
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens the gallery to select an image.
     */
    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    /**
     * Checks for required permissions before proceeding to open camera/gallery.
     */
    private fun checkPermissionsAndOpenOptions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            showOptionsDialog()
        }
    }

    /**
     * Updates the UI with the selected or captured image.
     */
    private fun updateUi(photoUri: Uri) {
        when (selectedPicker) {
            1 -> {  // Tenant photo
                binding.llSelectedTenantPhoto.ivThumbnail.setImageURI(photoUri)
                val fileName = CommonFunction().getFileName(this, photoUri)
                binding.llSelectedTenantPhoto.tvFileName.text = fileName
                val fileSize = CommonFunction().getFileSize(this, photoUri)
                binding.llSelectedTenantPhoto.tvFileSize.text = fileSize
                tenantImageUri = photoUri
                llTenantPhoto.visibility = View.VISIBLE
                binding.llAddTenantPhoto.visibility = View.GONE
            }
            2 -> {  // Front Aadhar photo
                binding.llSelectedFrontAdharPhoto.ivThumbnail.setImageURI(photoUri)
                val fileName = CommonFunction().getFileName(this, photoUri)
                binding.llSelectedFrontAdharPhoto.tvFileName.text = fileName
                val fileSize = CommonFunction().getFileSize(this, photoUri)
                binding.llSelectedFrontAdharPhoto.tvFileSize.text = fileSize
                frontAdharImageUri = photoUri
                llFrontAdharPhoto.visibility = View.VISIBLE
                binding.llAddFrontAdhar.visibility = View.GONE
            }
            3 -> {  // Back Aadhar photo
                binding.llSelectedBackAdharPhoto.ivThumbnail.setImageURI(photoUri)
                val fileName = CommonFunction().getFileName(this, photoUri)
                binding.llSelectedBackAdharPhoto.tvFileName.text = fileName
                val fileSize = CommonFunction().getFileSize(this, photoUri)
                binding.llSelectedBackAdharPhoto.tvFileSize.text = fileSize
                backAdharImageUri = photoUri
                llBackAdharPhoto.visibility = View.VISIBLE
                binding.llAddBackAdhar.visibility = View.GONE
            }
        }
    }


    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            when (selectedPicker) {
                1 -> tenantImageUri = it
                2 -> frontAdharImageUri = it
                3 -> backAdharImageUri = it
            }
            updateUi(it)
        } ?: Toast.makeText(this, "Failed to upload photo!", Toast.LENGTH_SHORT).show()
    }

    // ActivityResultLauncher for camera capture
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            when (selectedPicker) {
                1 -> tenantImageUri?.let { updateUi(it) } ?: Toast.makeText(this, "Failed to capture photo!", Toast.LENGTH_SHORT).show()
                2 -> frontAdharImageUri?.let { updateUi(it) } ?: Toast.makeText(this, "Failed to capture photo!", Toast.LENGTH_SHORT).show()
                3 -> backAdharImageUri?.let { updateUi(it) } ?: Toast.makeText(this, "Failed to capture photo!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Photo capture cancelled!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showOptionsDialog()
            } else {
                Toast.makeText(this, "Permissions are required to proceed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateDaysUntilDue(dueDateStr: String): Long {
        return try {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val dueDate = sdf.parse(dueDateStr) ?: return 1L
            val today = java.util.Date()
            val diff = dueDate.time - today.time
            val days = diff / (1000 * 60 * 60 * 24)
            if (days > 0) days else 1L
        } catch (e: Exception) {
            1L
        }
    }

    private fun createTenant() {

        Log.e("CREATE_TENANT", "========== CREATE REQUEST ==========")

        Log.e("CREATE_TENANT", "RoomId = ${selectedRoom?.id}")
        Log.e("CREATE_TENANT", "PropertyId = ${selectedProperty?.id}")

        Log.e("CREATE_TENANT", "TenantName = ${binding.etTenantName.text}")
        Log.e("CREATE_TENANT", "Phone = ${binding.etPhoneNumber.text}")

        Log.e("CREATE_TENANT", "Rent = ${binding.llRentFinanceDetail.etRentAmount.text}")

        Log.e("CREATE_TENANT", "Deposit = ${binding.llRentFinanceDetail.etDepositAmount.text}")

        Log.e("CREATE_TENANT", "MoveIn = ${binding.llRentFinanceDetail.tvMoveInDate.text}")

        Log.e("CREATE_TENANT", "RentStart = ${binding.llRentFinanceDetail.tvRentStartDate.text}")

        Log.e("CREATE_TENANT", "RentDue = ${binding.llRentFinanceDetail.tvRentDueDate.text}")




        viewModel.createTenant(

            roomId = selectedRoom?.id ?: "",

            tenantName = binding.etTenantName.text.toString().trim(),

            phoneNumber = binding.etPhoneNumber.text.toString().trim(),

            phoneCode = "+91",

            rent = binding.llRentFinanceDetail.etRentAmount.text.toString().trim(),

            roomDeposit = binding.llRentFinanceDetail.etDepositAmount.text.toString().trim(),

            checkinDate =
                binding.llRentFinanceDetail.tvMoveInDate.text.toString().trim(),

            rentStartDate =
                binding.llRentFinanceDetail.tvRentStartDate.text.toString().trim(),

            rentSubmissionDate =
                binding.llRentFinanceDetail.tvRentDueDate.text.toString().trim(),

            fixedWaterBill = when (
                binding.llWaterFinanceDetail.spWater.selectedItemPosition
            ) {
                1 -> "fix"
                2 -> "metered"
                else -> ""
            },

            fixedElectricity = when (
                binding.llElectricityFinanceDetail.spElectricity.selectedItemPosition
            ) {
                1 -> "fix"
                2 -> "metered"
                else -> ""
            },

            fixedWaterBillAmount =
                binding.llWaterFinanceDetail.etWaterFixedAmount.text.toString().trim(),

            fixedElectricityAmount =
                binding.llElectricityFinanceDetail.etElectricityDefaultAmount.text.toString().trim(),

            costPerUnit =
                binding.llElectricityFinanceDetail.etElectricityCostUnit.text.toString().trim(),

            meterReading = "",

            meterReadingWater = "",

            costUnitWater =
                binding.llWaterFinanceDetail.etWaterCostUnit.text.toString().trim(),

            referenceName =
                binding.etReferenceName.text.toString().trim(),


            profilePic =
                CommonFunction().getMultipartFromUri(
                    this,
                    tenantImageUri,
                    "profilePic"
                ),
            documents =
                CommonFunction().getMultipartListFromUris(
                    this,
                    listOfNotNull(
                        frontAdharImageUri,
                        backAdharImageUri
                    ),
                    "document"
                )

        )

    }

    private fun updateTenant() {

        val request = UpdateTenantRequest(

            propertyId = selectedProperty?.id ?: "",

            roomId = selectedRoom?.id ?: "",

            deposit = binding.llRentFinanceDetail.etDepositAmount.text.toString().trim(),

            tenantId = tenantId,

            tenantName = binding.etTenantName.text.toString().trim(),

            phoneNumber = binding.etPhoneNumber.text.toString().trim(),

            phoneCode = "+91",

            rent = binding.llRentFinanceDetail.etRentAmount.text.toString().trim(),

            roomDeposit = binding.llRentFinanceDetail.etDepositAmount.text.toString().trim(),

            rentStartDate = binding.llRentFinanceDetail.tvRentStartDate.text.toString().trim(),

            fixedWaterBillAmount =
                binding.llWaterFinanceDetail.etWaterFixedAmount.text.toString().trim(),

            fixedElectricityAmount =
                binding.llElectricityFinanceDetail.etElectricityDefaultAmount.text.toString().trim(),

            meterReading = "",

            waterReading = "",

            costPerUnit =
                binding.llElectricityFinanceDetail.etElectricityCostUnit.text.toString().trim(),

            costUnitWater =
                binding.llWaterFinanceDetail.etWaterCostUnit.text.toString().trim(),

            profilePic = tenantImageUri,

            documents = listOfNotNull(
                frontAdharImageUri,
                backAdharImageUri
            )

        )

        viewModel.updateTenant(request)

    }


    private fun showOccupiedRoomDialog(roomNo: String) {

        val dialog = android.app.Dialog(this)

        dialog.setContentView(R.layout.dialog_room_occupied)

        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(
                android.graphics.Color.TRANSPARENT
            )
        )

        dialog.setCancelable(false)

        val tvRoomNo =
            dialog.findViewById<TextView>(R.id.tvRoomNo)

        val tvMessage =
            dialog.findViewById<TextView>(R.id.tvMessage)

        val btnChoose =
            dialog.findViewById<Button>(R.id.btnAnotherRoom)

        val btnCancel =
            dialog.findViewById<Button>(R.id.btnCancel)

        tvRoomNo.text =
            "Room $roomNo"

        tvMessage.text =
            "This room is already occupied.\nPlease choose another room."

        btnChoose.setOnClickListener {

            binding.actRoom.setText("")

            dialog.dismiss()

            binding.actRoom.showDropDown()

        }

        btnCancel.setOnClickListener {

            binding.actRoom.setText("")

            dialog.dismiss()

        }

        dialog.show()
    }

    private fun scheduleRentReminder(
        tenantName: String,
        rentAmount: String,
        daysUntilDue: Long
    ) {
        val data = androidx.work.Data.Builder()
            .putString("tenant_name", tenantName)
            .putString("amount", rentAmount)
            .build()

        val request = androidx.work.OneTimeWorkRequestBuilder<com.xvantage.rental.utils.RentReminderWorker>()
            .setInitialDelay(daysUntilDue, java.util.concurrent.TimeUnit.DAYS)
            .setInputData(data)
            .build()

        androidx.work.WorkManager.getInstance(this).enqueue(request)

        Toast.makeText(
            this,
            "Rent reminder set for $tenantName",
            Toast.LENGTH_SHORT
        ).show()
    }
}
