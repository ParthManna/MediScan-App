package com.example.mediscan2

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mediscan2.databinding.ActivityMain4Binding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class MainActivity4 : AppCompatActivity() {

    private lateinit var binding: ActivityMain4Binding
    private lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMain4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Supabase client
        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://bwbabpydaarigkibyanp.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ3YmFicHlkYWFyaWdraWJ5YW5wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ0NDM4OTAsImV4cCI6MjA2MDAxOTg5MH0.S7M2oDhxKRWYfeuzsoeU0jke-CjYgINY-09kR-G9IT8"
        ) {
            install(Storage)
            install(GoTrue)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadLatestImageFromSupabase()
    }

    private fun loadLatestImageFromSupabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // List files in the bucket
                val files = supabaseClient.storage
                    .from("photos")
                    .list()
                    .sortedByDescending { it.createdAt }

                if (files.isNotEmpty()) {
                    val latestImage = files.first()


                    // Construct public URL manually (alternative to getPublicUrl)
                    val publicUrl = "https://${supabaseClient.supabaseUrl}/storage/v1/object/public/photos/${latestImage.name}"



                    // Or use the download URL if you need authenticated access
                    // val downloadUrl = supabaseClient.storage
                    //     .from("photos")
                    //     .createSignedUrl(latestImage.name, 60) // expires in 60 seconds



                    val url = URL(publicUrl)
                    val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                    withContext(Dispatchers.Main) {
                        binding.imageView.setImageBitmap(bitmap)



                        Toast.makeText(
                                this@MainActivity4,
                                "Loaded: ${latestImage.name}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity4,
                            "No images found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("Supabase", "Error loading image", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity4,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}