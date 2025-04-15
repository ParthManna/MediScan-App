package com.example.mediscan2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mediscan2.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://bwbabpydaarigkibyanp.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ3YmFicHlkYWFyaWdraWJ5YW5wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ0NDM4OTAsImV4cCI6MjA2MDAxOTg5MH0.S7M2oDhxKRWYfeuzsoeU0jke-CjYgINY-09kR-G9IT8"
        ) {
            install(Auth)
            install(Storage)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        handleDeepLinkIntent(intent)

        setupNavigation()

//        lifecycleScope.launch {
//            try {
//                val session = supabase.auth.currentSessionOrNull()
//                if (session == null) {
//                    startActivity(Intent(this@MainActivity, MainActivity5::class.java))
//                    finish()
//                    return@launch
//                }
//
//                // Continue with the rest of your setup
//                setupNavigation()
//            } catch (e: Exception) {
//                Log.e("MainActivity", "Auth check failed", e)
//                startActivity(Intent(this@MainActivity, MainActivity5::class.java))
//                finish()
//            }
//        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent) {
        val data = intent.data
        if (data != null && data.scheme == "mediscan" && data.host == "callback") {
            val accessToken = data.getQueryParameter("access_token")
            val type = data.getQueryParameter("type")
            if (!accessToken.isNullOrBlank()) {
                lifecycleScope.launch {
                    try {
                        supabase.auth.exchangeCodeForSession(accessToken)
                        Toast.makeText(this@MainActivity, "Email confirmed & logged in!", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun setupNavigation() {
        binding.appBarMain.fab.setOnClickListener { view ->
            AlertDialog.Builder(view.context)
                .setTitle("Customer Support")
                .setMessage("Choose an option to contact support")
                .setPositiveButton("Email Now") { _, _ ->
                    openEmailClient("partha.worklife@gmail.com")
                }
                .setNegativeButton("Call Now") { _, _ ->
                    openPhoneDialer("9641884426")
                }
                .show()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Set user email in nav header
        val headerView = navView.getHeaderView(0)
        val textViewUserEmail = headerView.findViewById<TextView>(R.id.textView)
        val textViewUserName = headerView.findViewById<TextView>(R.id.textViewName) // Ensure this TextView exists in your nav_header layout

        runBlocking {
            val user = supabase.auth.currentUserOrNull()
            val email = user?.email
            if (email != null) {
                val name = email.substringBefore('@').replaceFirstChar { it.uppercaseChar() }
                textViewUserName.text = name
                textViewUserEmail.text = email
            } else {
                textViewUserName.text = "Guest"
                textViewUserEmail.text = ""
            }
        }


        // Handle navigation item clicks
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logout()
                    true
                }
                else -> {
                    drawerLayout.closeDrawers()
                    menuItem.isChecked = true
                    navController.navigate(menuItem.itemId)
                    true
                }
            }
        }
    }

//    private fun isUserAuthenticated(): Boolean {
//        return runBlocking {
//            try {
//                supabase.auth.currentSessionOrNull() != null
//            } catch (e: Exception) {
//                false
//            }
//        }
//    }

    private fun redirectToAuth() {
        startActivity(Intent(this, MainActivity5::class.java))
        finish()
    }

    private fun logout() {
        runBlocking {
            try {
                supabase.auth.signOut()
                redirectToAuth()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openEmailClient(email: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        try {
            startActivity(emailIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "No email client available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPhoneDialer(phone: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL, "tel:${phone}".toUri())
        try {
            startActivity(dialIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "No dialer app available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}