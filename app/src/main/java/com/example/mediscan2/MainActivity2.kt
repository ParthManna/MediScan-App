package com.example.mediscan2

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mediscan2.databinding.ActivityMain2Binding
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
            startActivity(Intent(this, MainActivity4::class.java))
            ImagePass()

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

        }
    }

    private fun ImagePass(){
        // Save bitmap to cache directory
        val file = File(cacheDir, "shared_image.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()

        val imageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider", // add authority in manifest
            file
        )

        // Send intent with Uri
        val intent = Intent(this, MainActivity4::class.java).apply {
            putExtra("imageUri", imageUri.toString())
        }
        startActivity(intent)
    }
}
