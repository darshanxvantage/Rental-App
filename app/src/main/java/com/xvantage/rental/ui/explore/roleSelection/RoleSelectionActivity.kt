package com.xvantage.rental.ui.explore.roleSelection

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xvantage.rental.databinding.ActivityRoleSelectionBinding
import com.xvantage.rental.ui.explore.common.ExploreRoleManager
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardStudent.setOnClickListener {
            ExploreRoleManager.saveRole(this, ExploreRoleManager.ROLE_STUDENT)
            finish()
        }

        binding.cardOwner.setOnClickListener {
            ExploreRoleManager.saveRole(this, ExploreRoleManager.ROLE_OWNER)
            finish()
        }
    }
}