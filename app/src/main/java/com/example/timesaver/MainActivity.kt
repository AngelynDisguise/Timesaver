package com.example.timesaver

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.timesaver.database.TimesaverDatabase
import com.example.timesaver.database.TimesaverRepository
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val dao by lazy { TimesaverDatabase.getDatabase(applicationContext).timesaverDao() }
    val repository by lazy { TimesaverRepository(dao) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set up Nav Controller
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Set up Action Bar with Drawer Layout
        drawerLayout = findViewById(R.id.drawer_layout)
        appBarConfiguration = AppBarConfiguration(
            setOf( // the top destinations in the drawer
            R.id.main_fragment,
            R.id.settings_fragment,
            R.id.activity_menu_fragment
        ), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Set up Navigation View
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }

}