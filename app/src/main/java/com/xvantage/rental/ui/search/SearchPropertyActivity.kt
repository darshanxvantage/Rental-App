package com.xvantage.rental.ui.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xvantage.rental.databinding.ActivitySearchPropertyBinding

class SearchPropertyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchPropertyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivitySearchPropertyBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            finish()
        }
    }
}