package com.xvantage.rental.ui.dashboard

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.databinding.DataBindingUtil
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityPrivacyPolicyBinding
import com.xvantage.rental.ui.base.BaseActivity

/**
 * PrivacyPolicyActivity
 *
 * Privacy Policy ne app ni andar j (offline) batave che.
 * assets/privacy_policy.html file ne WebView ma load kare che,
 * eatle internet na hoy to pan Privacy Policy khuli jashe.
 */
class PrivacyPolicyActivity : BaseActivity() {

    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_privacy_policy)

        setupWebView()

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun setupWebView() {
        with(binding.webView) {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = false
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false

            binding.progressBar.visibility = View.VISIBLE
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.visibility = View.GONE
                }
            }

            // assets/privacy_policy.html file ne load kare che
            loadUrl("file:///android_asset/privacy_policy.html")
        }
    }

    override fun onDestroy() {
        binding.webView.destroy()
        super.onDestroy()
    }
}