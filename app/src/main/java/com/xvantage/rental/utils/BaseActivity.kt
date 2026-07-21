package com.xvantage.rental.utils

import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var progressDialog: ProgressDialog
    private lateinit var connectivityManager: ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initProgressDialog()
        monitorNetworkChanges()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })
    }

    private fun initProgressDialog() {
        progressDialog = ProgressDialog(this).apply {
            setMessage("Loading...")
            setCancelable(false)
        }
    }

    fun showProgressBar() {
        if (!progressDialog.isShowing) progressDialog.show()
    }

    fun hideProgressBar() {
        if (progressDialog.isShowing) progressDialog.dismiss()
    }

    private fun monitorNetworkChanges() {
        connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Toast removed intentionally — no "Network connected" message needed.
            }

            override fun onLost(network: Network) {
                runOnUiThread { showToast("Network disconnected") }
            }
        }

        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, networkCallback!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit App")
            .setMessage("Do you really want to exit?")
            .setPositiveButton("No") { dialog, _ -> dialog.dismiss() }
            .setNegativeButton("Yes") { _, _ -> finishAffinity() }
            .setCancelable(false)
            .show()
    }
}
