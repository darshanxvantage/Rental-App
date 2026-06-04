package com.xvantage.rental.ui.dashboard.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.viewModels


@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var appPreference: AppPreference
    private val viewModel: AuthViewModel by viewModels()

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

        // USER DATA

        binding.tvUserName.text =
            appPreference.getUserName()

        binding.tvPhone.text =
            appPreference.getPhone()

        binding.tvEmail.text =
            appPreference.getEmail()

        binding.tvCity.text =
            appPreference.getCity()

        binding.tvAge.text =
            appPreference.getAge()

        binding.tvListedCount.text =
            appPreference.getListedCount().toString()

        binding.tvRentedCount.text =
            appPreference.getRentedCount().toString()

        binding.tvRating.text =
            "${appPreference.getRating()}★"

        // PROFILE IMAGE

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
                }
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }


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

                    binding.tvCity.text =
                        "📍 City : $city"

                    binding.tvAge.text =
                        "🎂 Age : $age"

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

                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        90,
                        outputStream
                    )

                    outputStream.flush()

                    outputStream.close()

                    binding.imgProfile.setImageBitmap(
                        bitmap
                    )

                    appPreference.setProfileImage(
                        file.absolutePath
                    )
//                    viewModel.updateProfileImage(
//                        appPreference.getUserName() ?: "User",
//                        file
//                    )
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

