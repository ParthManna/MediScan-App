package com.example.mediscan2

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mediscan2.databinding.ActivityMain4Binding
import androidx.core.net.toUri
import com.google.firebase.database.FirebaseDatabase

class MainActivity4 : AppCompatActivity() {

    private lateinit var binding: ActivityMain4Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMain4Binding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadImageFromFirebase()

    }

    private fun loadImageFromFirebase() {
        val dbRef = FirebaseDatabase.getInstance().getReference("images/sharedImage")
        dbRef.get().addOnSuccessListener { snapshot ->
            val imageString = snapshot.getValue(String::class.java)
            if (!imageString.isNullOrEmpty()) {
                val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.imageView.setImageBitmap(bitmap)
            }
        }.addOnFailureListener {
            // Handle failure
        }
    }
}