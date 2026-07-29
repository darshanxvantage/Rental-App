package com.xvantage.rental.ui.explore.roleSelection

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.xvantage.rental.data.source.ExploreRepository
import com.xvantage.rental.databinding.ActivityRoleSelectionBinding
import com.xvantage.rental.network.utils.ResultWrapper
import com.xvantage.rental.ui.dashboard.DashboardActivity
import com.xvantage.rental.ui.explore.ExploreActivity
import com.xvantage.rental.ui.explore.common.ExploreRoleManager
import com.xvantage.rental.ui.explore.createListing.CreateListingActivity
import com.xvantage.rental.utils.AppPreference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    @Inject
    lateinit var exploreRepository: ExploreRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardStudent.setOnClickListener {
            ExploreRoleManager.saveRole(this, ExploreRoleManager.ROLE_STUDENT)
            onRoleSaved()
        }

        binding.cardOwner.setOnClickListener {
            ExploreRoleManager.saveRole(this, ExploreRoleManager.ROLE_OWNER)
            onRoleSaved()
        }
    }

    private fun onRoleSaved() {
        val appPreference = AppPreference(this)
        if (!appPreference.isProfileComplete()) {
            finish()
            return
        }

        when (ExploreRoleManager.getRole(this)) {
            ExploreRoleManager.ROLE_STUDENT -> {
                startActivity(Intent(this, ExploreActivity::class.java))
                finish()
            }
            ExploreRoleManager.ROLE_OWNER -> {
                lifecycleScope.launch {
                    val hasListing = when (
                        val result = exploreRepository.myListings(currentPage = 1, pageSize = 1)
                    ) {
                        is ResultWrapper.Success ->
                            (result.value.data?.rows?.isNotEmpty() == true)
                        else -> true // network hiccup: don't force onboarding again, land on Dashboard
                    }
                    if (hasListing) {
                        startActivity(Intent(this@RoleSelectionActivity, DashboardActivity::class.java))
                    } else {
                        CreateListingActivity.startForOnboarding(this@RoleSelectionActivity)
                    }
                    finish()
                }
            }
            else -> finish() // shouldn't happen - we just saved one above
        }
    }
}