package com.xvantage.rental.ui.auth.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.xvantage.rental.R
import com.xvantage.rental.databinding.FragmentSignUpBinding
import com.xvantage.rental.ui.auth.AuthViewModel
import com.xvantage.rental.ui.auth.fragment.sealed.AuthScreen
import com.xvantage.rental.ui.auth.fragment.sealed.AuthState
import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : BaseFragment() {

    private lateinit var layoutBinding: FragmentSignUpBinding

    private lateinit var appPreference: AppPreference

    private val viewModel: AuthViewModel
            by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        layoutBinding =
            FragmentSignUpBinding.inflate(
                inflater,
                container,
                false
            )

        return layoutBinding.root
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

        layoutBinding.btnNext.setOnClickListener {

            val phone =
                layoutBinding.etPhone.text
                    .toString()
                    .trim()

            if (phone.length == 10) {

                viewModel.signUp(phone)

            } else {

                Toast.makeText(
                    context,
                    "Enter valid phone number",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        layoutBinding.tvSignIn.setOnClickListener {

            viewModel.setCurrentScreen(
                AuthScreen.SignIn
            )
        }

        observeState()
    }

    private fun observeState() {

        lifecycleScope.launch {

            viewModel.authState.collect {

                when (it) {

                    is AuthState.Error -> {

                        if (
                            it.error.contains(
                                "already registered",
                                true
                            )
                        ) {

                            val dialogView =
                                layoutInflater.inflate(
                                    R.layout.dialog_already_registered,
                                    null
                                )

                            val dialog =
                                AlertDialog.Builder(
                                    requireContext()
                                )
                                    .setView(dialogView)
                                    .create()

                            dialog.window?.setBackgroundDrawableResource(
                                android.R.color.transparent
                            )

                            dialog.show()

                            val btnLoginNow =
                                dialogView.findViewById<MaterialButton>(
                                    R.id.btnLoginNow
                                )

                            btnLoginNow.setOnClickListener {

                                dialog.dismiss()

                                viewModel.setCurrentScreen(
                                    AuthScreen.SignIn
                                )
                            }

                        } else {

                            Toast.makeText(
                                context,
                                it.error,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}

