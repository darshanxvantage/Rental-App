package com.xvantage.rental.ui.auth.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.xvantage.rental.databinding.FragmentCreateProfileBinding
import com.xvantage.rental.ui.auth.AuthViewModel
import com.xvantage.rental.ui.auth.fragment.sealed.AuthState
import com.xvantage.rental.ui.dashboard.DashboardActivity
import com.xvantage.rental.utils.AppPreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateProfileFragment : Fragment() {

    private lateinit var binding: FragmentCreateProfileBinding

    private lateinit var appPreference: AppPreference


    private val viewModel: AuthViewModel
            by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            FragmentCreateProfileBinding.inflate(
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


        // CREATE PROFILE

        binding.btnCreateProfile.setOnClickListener {

            val firstName =
                binding.etFirstName.text
                    .toString()
                    .trim()

            val lastName =
                binding.etLastName.text
                    .toString()
                    .trim()

            val email =
                binding.etEmail.text
                    .toString()
                    .trim()

            val state =
                binding.etState.text
                    .toString()
                    .trim()

            val city =
                binding.etCity.text
                    .toString()
                    .trim()

            val age =
                binding.etAge.text
                    .toString()
                    .trim()

            if (
                firstName.isEmpty() ||
                lastName.isEmpty() ||
                email.isEmpty() ||
                state.isEmpty() ||
                city.isEmpty() ||
                age.isEmpty()
            ) {

                Toast.makeText(
                    context,
                    "Fill all details",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            viewModel.createProfile(
                firstName,
                lastName,
                email,
                state,
                city,
                age.toInt()
            )
        }

        observeState()
    }

    private fun observeState() {

        lifecycleScope.launch {

            viewModel.authState.collect {

                when (it) {

                    is AuthState.Success -> {

                        // SAVE USER DATA

                        appPreference.setUserName(
                            "${binding.etFirstName.text} ${binding.etLastName.text}"
                        )

                        appPreference.setEmail(
                            binding.etEmail.text.toString()
                        )

                        appPreference.setCity(
                            binding.etCity.text.toString()
                        )

                        appPreference.setState(
                            binding.etState.text.toString()
                        )

                        appPreference.setAge(
                            binding.etAge.text.toString()
                        )
                        Toast.makeText(
                            context,
                            "Profile Created Successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(
                            Intent(
                                requireContext(),
                                DashboardActivity::class.java
                            )
                        )

                        requireActivity().finish()
                    }

                    is AuthState.Error -> {

                        Toast.makeText(
                            context,
                            it.error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> Unit
                }
            }
        }
    }
}