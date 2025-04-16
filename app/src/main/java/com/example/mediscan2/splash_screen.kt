package com.example.mediscan2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mediscan2.databinding.ActivitySplashScreenBinding

class splash_screen : AppCompatActivity() {
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

        // Start the animation
        val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.splash_zoom)
        binding.splashImage.startAnimation(anim)

        // Navigate after animation ends
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToMainActivity()
        }, 2000)
    }


    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity5::class.java)
        startActivity(intent)
        finish()
    }
}
