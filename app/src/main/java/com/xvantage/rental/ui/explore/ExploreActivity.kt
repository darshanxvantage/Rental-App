package com.xvantage.rental.ui.explore

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.xvantage.rental.R
import com.xvantage.rental.databinding.ActivityExploreBinding
import com.xvantage.rental.ui.explore.common.ExploreRoleManager
import com.xvantage.rental.ui.explore.discover.DiscoverFragment
import com.xvantage.rental.ui.explore.favorites.FavoritesFragment
import com.xvantage.rental.ui.explore.myListings.MyListingsActivity
import com.xvantage.rental.ui.explore.profile.ExploreProfileFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Host activity for the whole Explore feature (students AND owners land
 * here once they pick a role on RoleSelectionActivity). Deliberately
 * mirrors DashboardActivity's own pattern: BottomNavigationView + manual
 * supportFragmentManager.replace(...) - no NavHostFragment.
 */
@AndroidEntryPoint
class ExploreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExploreBinding
    private var isOwner: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isOwner = ExploreRoleManager.isOwner(this)

        setupBottomNavigation()
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_discover
            loadFragment(DiscoverFragment.newInstance())
        }
    }

    private fun setupBottomNavigation() {
        // "My Listings" tab only makes sense for property owners
        binding.bottomNavigation.menu.findItem(R.id.nav_my_listings).isVisible = isOwner

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_discover -> {
                    loadFragment(DiscoverFragment.newInstance())
                    true
                }
                R.id.nav_favorites -> {
                    loadFragment(FavoritesFragment.newInstance())
                    true
                }
                R.id.nav_my_listings -> {
                    // MyListingsActivity is a separate Activity (owner's own
                    // listing management), not an embedded tab fragment.
                    startActivity(Intent(this, MyListingsActivity::class.java))
                    false // don't keep this tab visually "selected" - it's a different screen
                }
                R.id.nav_profile -> {
                    loadFragment(ExploreProfileFragment.newInstance())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit()
    }
}