package com.example.mediscan2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.mediscan2.databinding.ActivitySplashScreenBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.user.UserSession
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class splash_screen : AppCompatActivity() {

    private val supabase: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = "https://bwbabpydaarigkibyanp.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ3YmFicHlkYWFyaWdraWJ5YW5wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ0NDM4OTAsImV4cCI6MjA2MDAxOTg5MH0.S7M2oDhxKRWYfeuzsoeU0jke-CjYgINY-09kR-G9IT8"
        ) {
            install(Auth){
                alwaysAutoRefresh = true
                autoLoadFromStorage = true
            }
            install(Storage)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




        val anim2 = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.zoom_in_out)
        binding.splashImage.startAnimation(anim2)

        // Navigate after animation ends
        Handler(Looper.getMainLooper()).postDelayed({
            Authentication()
        }, 3000)

    }

    @OptIn(UnstableApi::class)
    private fun Authentication() {

        // Check authentication state with proper waiting
        lifecycleScope.launch {
            try {
                // Wait for session to be loaded (with timeout)
                val session = waitForSessionLoad()

                if (session == null) {
                    Log.d("MainActivity", "No valid session after waiting, redirecting to auth")
                    startActivity(Intent(this@splash_screen, MainActivity5::class.java))
                    finish()
                } else {
                    Log.d("MainActivity", "Valid session found: ${session.user?.email}")
                    navigateToMainActivity()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Auth check failed", e)
                startActivity(Intent(this@splash_screen, MainActivity5::class.java))
                finish()
            }
        }
    }

    private suspend fun waitForSessionLoad(timeoutMillis: Long = 1000L): UserSession? {
        return withTimeoutOrNull(timeoutMillis) {
            var session = supabase.auth.currentSessionOrNull()
            while (session == null) {
                delay(20) // Small delay between checks
                session = supabase.auth.currentSessionOrNull()
            }
            session
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this@splash_screen, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}

