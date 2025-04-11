package com.example.mediscan2

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mediscan2.databinding.ActivityMain2Binding
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    private lateinit var bitmap : Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.topAppBar)


        binding.analyzeBtn.setOnClickListener {
            uploadImageToFirebase()
            startActivity(Intent(this, MainActivity4::class.java))

        }

        binding.selectImageBtn.setOnClickListener {

            var intent = Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            startActivityForResult(intent, 100)
        }

        binding.cancelBtn.setOnClickListener {
            binding.uploadIcon.visibility = View.GONE
            binding.Icon.visibility = View.VISIBLE
        }




    }


    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data?.data != null) {
            val uri = data.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

            binding.uploadIcon.visibility = View.VISIBLE
            binding.Icon.visibility = View.GONE
            binding.uploadIcon.setImageBitmap(bitmap)



//            // Convert bitmap to Base64
//            val byteArrayOutputStream = ByteArrayOutputStream()
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
//            val imageBytes = byteArrayOutputStream.toByteArray()
//            val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)
//
//            // Store in Firebase
//            FirebaseDatabase.getInstance().getReference("images").push().setValue(imageString)




            
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
