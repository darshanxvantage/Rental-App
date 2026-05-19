package com.xvantage.rental.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityAuthBinding
import com.xvantage.rental.ui.auth.fragment.sealed.AuthScreen
import com.xvantage.rental.ui.auth.fragment.sealed.AuthState
import com.xvantage.rental.utils.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.content.Intent
import com.xvantage.rental.ui.dashboard.DashboardActivity


@AndroidEntryPoint
class AuthActivity : BaseActivity() {

    private lateinit var navController: NavController
    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.auth_fragments_container) as NavHostFragment
        navController = navHostFragment.navController

        observeAuthState()
        observeCurrentScreen()
    }

    private fun observeAuthState() {
        lifecycleScope.launch {
            viewModel.authState.collect { state ->
                when (state) {
                    is AuthState.Loading -> showLoading(true)
                    is AuthState.Success -> showLoading(false)
                    is AuthState.Error -> {
                        showLoading(false)
                        Toast.makeText(this@AuthActivity, state.error, Toast.LENGTH_SHORT).show()
                    }
                    AuthState.Idle -> showLoading(false)
                }
            }
        }
    }

    private fun observeCurrentScreen() {
        lifecycleScope.launch {
            viewModel.currentScreen.collect { screen ->
                when (screen) {
                    is AuthScreen.SignIn -> {
                        if (navController.currentDestination?.id
                            != R.id.signInFragment) {
                            navController.navigate(R.id.signInFragment)
                        }
                    }
                    is AuthScreen.SignUp -> {
                        if (navController.currentDestination?.id
                            != R.id.signUpFragment) {
                            navController.navigate(R.id.signUpFragment)
                        }
                    }
                    is AuthScreen.VerifyOtp -> {
                        if (navController.currentDestination?.id
                            != R.id.verifyOtpFragment) {
                            val bundle = Bundle().apply {
                                putString("email", screen.email)
                            }
                            navController.navigate(R.id.verifyOtpFragment, bundle)
                        }
                    }
                    is AuthScreen.ForgotPassword -> {
                        if (navController.currentDestination?.id
                            != R.id.forgotPasswordFragment) {
                            navController.navigate(R.id.forgotPasswordFragment)
                        }
                    }
                    is AuthScreen.Dashboard -> {
                        startActivity(
                            Intent(this@AuthActivity, DashboardActivity::class.java)
                        )
                        finish()
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

}
