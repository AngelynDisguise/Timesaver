package com.example.timesaver

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.timesaver.database.TimesaverDatabase
import com.example.timesaver.database.TimesaverRepository
import com.example.timesaver.fragments.MainFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val dao by lazy { TimesaverDatabase.getDatabase(applicationContext).timesaverDao() }
    val viewModel by lazy {
        ViewModelProvider(
            this,
            MainViewModelFactory(TimesaverRepository(dao))
        ) [MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(
            "MainActivity",
            "MainActivity created"
        )

        // Set up toolbar with the hamburger icon
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        /** Set up Nav Controller to host the fragments */
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up app bar with drawer layout, connect to NavController
        drawerLayout = findViewById(R.id.drawer_layout)
        appBarConfiguration = AppBarConfiguration(
            setOf( // the top destinations in the drawer
            R.id.main_fragment,
            R.id.settings_fragment,
            R.id.activity_menu_fragment
        ), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Connect the drawer UI (the navigation view) to NavController
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setupWithNavController(navController)

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.main_fragment, R.id.settings_fragment, R.id.activity_menu_fragment -> {
                    navController.navigate(menuItem.itemId, null, NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(R.id.main_fragment, false)
                        .build())
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}