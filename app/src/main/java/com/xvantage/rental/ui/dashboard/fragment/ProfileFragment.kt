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

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import java.io.File
import java.io.FileOutputStream



class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    private lateinit var appPreference: AppPreference

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
            "📍 City : ${appPreference.getCity()}"

        binding.tvAge.text =
            "🎂 Age : ${appPreference.getAge()}"

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

            Toast.makeText(
                context,
                "Edit Profile Coming Soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        // LOGOUT BUTTON

        binding.btnLogout.setOnClickListener {

            appPreference.clearPreferences()

            startActivity(
                Intent(
                    requireContext(),
                    AuthActivity::class.java
                )
            )

            requireActivity().finish()
        }
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
                }

            } catch (e: Exception) {

                e.printStackTrace()

                Toast.makeText(
                    context,
                    "Image Load Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }


        }
    }
}

