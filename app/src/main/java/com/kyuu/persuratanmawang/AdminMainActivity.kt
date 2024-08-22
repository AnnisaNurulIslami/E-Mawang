package com.kyuu.persuratanmawang

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kyuu.persuratanmawang.databinding.ActivityAdminMainBinding

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navViewAdmin
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main_admin) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home_admin,
                R.id.navigation_dashboard_admin,
                R.id.navigation_notifications_admin
            )
        )
        navView.setupWithNavController(navController)

        val openFragment = intent.getStringExtra("openFragment")
        if (openFragment == "NotificationsAdminFragment") {
            Log.d("AdminMainActivity", "Navigating to NotificationsAdminFragment")
            navView.post {
                navController.navigate(R.id.navigation_notifications_admin)
            }
        }

        val openFragmentHome = intent.getStringExtra("openFragment")
        if (openFragmentHome == "HomeAdminFragment") {
            Log.d("AdminMainActivity", "Navigating to NotificationsAdminFragment")
            navView.post {
                navController.navigate(R.id.navigation_dashboard_admin)
            }
        }

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home_admin -> {
                    navController.navigate(R.id.navigation_home_admin)
                    true
                }
                R.id.navigation_dashboard_admin -> {
                    navController.navigate(R.id.navigation_dashboard_admin)
                    true
                }
                R.id.navigation_notifications_admin -> {
                    navController.navigate(R.id.navigation_notifications_admin)
                    true
                }
                else -> false
            }
        }
    }
}
