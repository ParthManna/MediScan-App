package com.example.mediscan2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mediscan2.databinding.ActivityMain3Binding
import java.io.File
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream


class MainActivity3 : AppCompatActivity() {

    private lateinit var binding: ActivityMain3Binding
    private lateinit var bitmap: Bitmap

    companion object {
        const val CAMERA_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.topAppBar)

        binding.analyzeBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity4::class.java))
            uploadImageToFirebase()
        }

        binding.selectImageBtn.setOnClickListener {
            checkCameraPermissionAndOpenCamera()
        }

        binding.cancelBtn.setOnClickListener {
            binding.uploadIcon.visibility = View.GONE
            binding.Icon.visibility = View.VISIBLE
        }



    }

    private fun checkCameraPermissionAndOpenCamera() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), CAMERA_REQUEST_CODE)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    @Deprecated("Use ActivityResultLauncher instead on newer APIs")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val img = data?.extras?.get("data") as? Bitmap

            img?.let {
                bitmap = it
                binding.uploadIcon.visibility = View.VISIBLE
                binding.Icon.visibility = View.GONE
                binding.uploadIcon.setImageBitmap(it)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        }
    }

    private fun uploadImageToFirebase() {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        val dbRef = FirebaseDatabase.getInstance().getReference("images")
        dbRef.child("sharedImage").setValue(imageString)
    }
}