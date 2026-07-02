package com.xvantage.rental.ui.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xvantage.rental.databinding.ActivitySearchPropertyBinding
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.widget.addTextChangedListener
import com.xvantage.rental.network.response.PropertyItem
import com.xvantage.rental.ui.dashboard.PropertyListViewModel
import com.xvantage.rental.ui.dashboard.fragment.adapter.PropertiesAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import android.view.View


@AndroidEntryPoint
class SearchPropertyActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchPropertyBinding

    private val viewModel: PropertyListViewModel by viewModels()

    private lateinit var propertiesAdapter: PropertiesAdapter

    private var originalList =
        mutableListOf<PropertyItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivitySearchPropertyBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.ivClear.setOnClickListener {
            binding.etSearch.setText("")
        }

        propertiesAdapter = PropertiesAdapter(this)

        binding.rvProperties.layoutManager =
            LinearLayoutManager(this)

        binding.rvProperties.adapter =
            propertiesAdapter

        observeData()

        viewModel.loadProperties()

        binding.etSearch.addTextChangedListener {

            val query = it.toString()

            binding.ivClear.visibility =
                if (query.isEmpty())
                    View.GONE
                else
                    View.VISIBLE

            filterProperties(query)
        }
    }

    private fun observeData() {

        lifecycleScope.launch {

            viewModel.propertyList.collect {

                android.util.Log.e(
                    "SEARCH_PROPERTY",
                    "Count = ${it.size}"
                )

                originalList.clear()

                originalList.addAll(it)

                propertiesAdapter.addItems(emptyList())

                binding.rvProperties.visibility = View.GONE
            }
        }
    }

    private fun filterProperties(query: String) {

        if (query.trim().isEmpty()) {

            binding.rvProperties.visibility = View.GONE

            propertiesAdapter.addItems(emptyList())

            return
        }

        val filteredList = originalList.filter {

            it.name.contains(query, true) ||

                    it.address.contains(query, true)
        }

        if (filteredList.isEmpty()) {

            binding.rvProperties.visibility = View.GONE

        } else {

            binding.rvProperties.visibility = View.VISIBLE

        }

        propertiesAdapter.addItems(filteredList)
    }
}