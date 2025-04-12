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
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mediscan2.databinding.ActivityMain3Binding
import java.io.File
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException


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
        }

        binding.selectImageBtn.setOnClickListener {
            checkCameraPermissionAndOpenCamera()


        }

        binding.cancelBtn.setOnClickListener {
            binding.uploadIcon.visibility = View.GONE
            binding.Icon.visibility = View.VISIBLE
        }



    }


    private fun uploadImageToSupabase(imageUri: Uri) {
        val contentResolver = applicationContext.contentResolver
        val inputStream = contentResolver.openInputStream(imageUri)
        val fileName = "image_${System.currentTimeMillis()}.jpg"
        val fileBytes = inputStream?.readBytes()

        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

        binding.uploadIcon.visibility = View.VISIBLE
        binding.Icon.visibility = View.GONE
        binding.uploadIcon.setImageBitmap(bitmap)

        if (fileBytes == null) {
            println("Image read failed")
            return
        }

        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), fileBytes)

        val request = Request.Builder()
            .url("https://bwbabpydaarigkibyanp.supabase.co/storage/v1/object/photos/$fileName")
            .header("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ3YmFicHlkYWFyaWdraWJ5YW5wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ0NDM4OTAsImV4cCI6MjA2MDAxOTg5MH0.S7M2oDhxKRWYfeuzsoeU0jke-CjYgINY-09kR-G9IT8")
            .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ3YmFicHlkYWFyaWdraWJ5YW5wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ0NDM4OTAsImV4cCI6MjA2MDAxOTg5MH0.S7M2oDhxKRWYfeuzsoeU0jke-CjYgINY-09kR-G9IT8")
            .header("Content-Type", "image/jpeg")
            .put(requestBody)
            .build()

        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity3, "Upload success!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity3, "Upload failed: ${response.code}", Toast.LENGTH_SHORT).show()
                        Log.e("UploadError", response.body?.string() ?: "No error body")

                    }
                }
            }
        })
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

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val img = data?.extras?.get("data") as? Bitmap
            img?.let {
                bitmap = it
                // Save bitmap to temp file to get a Uri
                val file = File(cacheDir, "captured_image_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                val imageUri = FileProvider.getUriForFile(
                    this,
                    "${applicationContext.packageName}.fileprovider",
                    file
                )

                // Now we can upload the image using this Uri
                uploadImageToSupabase(imageUri)

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

//    private fun uploadImageToFirebase() {
//        val storageRef = FirebaseStorage.getInstance().reference
//        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
//
//        val byteArrayOutputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
//        val imageBytes = byteArrayOutputStream.toByteArray()
//
//        val uploadTask = imageRef.putBytes(imageBytes)
//        uploadTask.addOnSuccessListener {
//            imageRef.downloadUrl.addOnSuccessListener { uri ->
//                // You can store the download URL in Realtime Database or Firestore if needed
//            }
//        }.addOnFailureListener {
//            // Handle failed upload
//        }
//    }


//    private fun uploadImageToSupabase(imageUri: Uri) {
//        val contentResolver = applicationContext.contentResolver
//        val inputStream = contentResolver.openInputStream(imageUri)
//        val fileName = "image_${System.currentTimeMillis()}.jpg"
//        val fileBytes = inputStream?.readBytes()
//
//        if (fileBytes == null) {
//            println("Image read failed")
//            return
//        }
//
//        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), fileBytes)
//
//        val request = Request.Builder()
//            .url("https://bwbabpydaarigkibyanp.supabase.co/storage/v1/object/photos/$fileName")
//            .header("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ3YmFicHlkYWFyaWdraWJ5YW5wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ0NDM4OTAsImV4cCI6MjA2MDAxOTg5MH0.S7M2oDhxKRWYfeuzsoeU0jke-CjYgINY-09kR-G9IT8")
//            .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ3YmFicHlkYWFyaWdraWJ5YW5wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ0NDM4OTAsImV4cCI6MjA2MDAxOTg5MH0.S7M2oDhxKRWYfeuzsoeU0jke-CjYgINY-09kR-G9IT8")
//            .header("Content-Type", "image/jpeg")
//            .put(requestBody)
//            .build()
//
//        val client = OkHttpClient()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                e.printStackTrace()
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                if (response.isSuccessful) {
//                    runOnUiThread {
//                        Toast.makeText(this@MainActivity3, "Upload success!", Toast.LENGTH_SHORT).show()
//                    }
//                } else {
//                    runOnUiThread {
//                        Toast.makeText(this@MainActivity3, "Upload failed: ${response.code}", Toast.LENGTH_SHORT).show()
//                        Log.e("UploadError", response.body?.string() ?: "No error body")
//
//                    }
//                }
//            }
//        })
//    }


}