package com.xvantage.rental.ui.explore.roleSelection

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xvantage.rental.databinding.ActivityRoleSelectionBinding
import com.xvantage.rental.ui.dashboard.DashboardActivity
import com.xvantage.rental.ui.explore.ExploreActivity
import com.xvantage.rental.ui.explore.common.ExploreRoleManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * Shown once, right after login/profile-creation (see the integration note
 * in the structure plan - this is the "1-line redirect change" point).
 * Student -> ExploreActivity. Property Owner -> existing DashboardActivity
 * (completely untouched).
 */
@AndroidEntryPoint
class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardStudent.setOnClickListener {
            ExploreRoleManager.saveRole(this, ExploreRoleManager.ROLE_STUDENT)
            startActivity(Intent(this, ExploreActivity::class.java))
            finish()
        }

        binding.cardOwner.setOnClickListener {
            ExploreRoleManager.saveRole(this, ExploreRoleManager.ROLE_OWNER)
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}