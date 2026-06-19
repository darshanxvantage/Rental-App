package com.xvantage.rental.ui.auth.fragment

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope

import com.xvantage.rental.databinding.FragmentLoginBinding

import com.xvantage.rental.ui.auth.fragment.sealed.AuthState
import com.xvantage.rental.ui.auth.AuthViewModel


import com.xvantage.rental.utils.AppPreference
import com.xvantage.rental.utils.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LoginFragment : BaseFragment() {
    private lateinit var layoutBinding: FragmentLoginBinding
    private lateinit var appPreference: AppPreference
    private val viewModel: AuthViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutBinding = FragmentLoginBinding.inflate(inflater, container, false)
        return layoutBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appPreference = AppPreference(requireContext())

        layoutBinding.btnSignin.setOnClickListener {
            val phone = layoutBinding.etPhone.text.toString().trim()

            // Clear previous error
            layoutBinding.etPhone.error = null

            when {
                phone.isEmpty() -> {
                    layoutBinding.etPhone.error = "⚠ Phone number is required"
                    layoutBinding.etPhone.requestFocus()
                }
                !phone.all { it.isDigit() } -> {
                    layoutBinding.etPhone.error = "⚠ Only numbers allowed"
                    layoutBinding.etPhone.requestFocus()
                }
                phone.length != 10 -> {
                    layoutBinding.etPhone.error = "⚠ Enter valid 10 digit number"
                    layoutBinding.etPhone.requestFocus()
                }
//                phone.first() !in listOf('6', '7', '8', '9') -> {
//                    layoutBinding.etPhone.error = "⚠ Enter valid Indian mobile number"
//                    layoutBinding.etPhone.requestFocus()
//                }
                else -> {
                    viewModel.signIn(phone)
                }
            }
        }

        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.authState.collect {
                when (it) {
                    is AuthState.Error -> {
                        Toast.makeText(context, it.error, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}