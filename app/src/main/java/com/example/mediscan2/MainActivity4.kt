package com.example.mediscan2

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mediscan2.databinding.ActivityMain4Binding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.net.URL

class MainActivity4 : AppCompatActivity() {

    private lateinit var binding: ActivityMain4Binding
    private lateinit var supabaseClient: SupabaseClient
    private lateinit var bitmap: Bitmap
    private var currentPrediction: String = ""
//    private lateinit var module: Module

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMain4Binding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.learnmore.isEnabled = false

//        // Load PyTorch model
//        try {
//            module = LiteModuleLoader.load(assetFilePath("skin_disease.ptl"))
//        } catch (e: Exception) {
//            Log.e("PyTorch", "Model load error", e)
//            Toast.makeText(this, "Model load error", Toast.LENGTH_LONG).show()
//            return
//        }

        // Supabase Init
        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://bwbabpydaarigkibyanp.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJ3YmFicHlkYWFyaWdraWJ5YW5wIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDQ0NDM4OTAsImV4cCI6MjA2MDAxOTg5MH0.S7M2oDhxKRWYfeuzsoeU0jke-CjYgINY-09kR-G9IT8"
        ) {
            install(Storage)
            install(Auth)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.learnmore.setOnClickListener {
            when (currentPrediction.lowercase()) {
                "acne" -> startActivity(Intent(this@MainActivity4, MainActivity6::class.java))
                "eczema" -> startActivity(Intent(this@MainActivity4, MainActivity7::class.java))
                "psoriasis" -> startActivity(Intent(this@MainActivity4, MainActivity8::class.java))
                "warts" -> startActivity(Intent(this@MainActivity4, MainActivity9::class.java))
                "skincancer" -> startActivity(Intent(this@MainActivity4, MainActivity10::class.java))
                else -> {
                    // Fallback option
                    val intent = Intent(Intent.ACTION_VIEW,
                        "https://en.wikipedia.org/wiki/Natural_(disambiguation)".toUri())
                    startActivity(intent)
                }
            }
        }

        loadLatestImageFromSupabase()
    }

    private fun loadLatestImageFromSupabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val files = supabaseClient.storage
                    .from("photos")
                    .list()
                    .sortedByDescending { it.createdAt }

                if (files.isNotEmpty()) {
                    val latestImage = files.first()
                    val publicUrl = "https://${supabaseClient.supabaseUrl}/storage/v1/object/public/photos/${latestImage.name}"
                    val url = URL(publicUrl)
                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())

                    withContext(Dispatchers.Main) {
                        binding.imageView.setImageBitmap(bitmap)
                        Toast.makeText(
                            this@MainActivity4,
                            "Loaded: ${latestImage.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                        runModelInference(bitmap)
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

    private fun runModelInference(bitmap: Bitmap) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://skin-disease-api-j0l8.onrender.com/predict/"

                // Convert bitmap to JPEG byte array
                val stream = java.io.ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val imageBytes = stream.toByteArray()

                val client = okhttp3.OkHttpClient()
                val requestBody = okhttp3.MultipartBody.Builder()
                    .setType(okhttp3.MultipartBody.FORM)
                    .addFormDataPart(
                        "file", "image.jpg",
                        okhttp3.RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)
                    )
                    .build()

                val request = okhttp3.Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val json = response.body?.string()
                val jsonObject = JSONObject(json ?: "{}")

                val prediction = jsonObject.optString("prediction", "Unknown")
                val confidences = jsonObject.optJSONObject("confidence_percentages")
                currentPrediction = prediction // Store the prediction


                // Build the display string
                val resultString = StringBuilder()
                resultString.append("Prediction: $prediction\nConfidence Percentages:\n")

                confidences?.let {
//                    // Sort by confidence percentage (descending)
//                    val sortedEntries = mutableListOf<Pair<String, Double>>()
//                    for (key in it.keys()) {
//                        sortedEntries.add(Pair(key, it.getDouble(key)))
//                    }
//                    sortedEntries.sortByDescending { it.second }
//
//                    // Add each entry to the result string
//                    sortedEntries.forEach { (disease, percentage) ->
//                        resultString.append("${disease}: ${"%.2f".format(percentage)}%\n")
//                    }


                    confidences.let {
                        // Find the entry with highest confidence
                        var topDisease = ""
                        var topConfidence = 0.0

                        for (key in it.keys()) {
                            val confidence = it.getDouble(key)
                            if (confidence > topConfidence) {
                                topDisease = key
                                topConfidence = confidence
                            }
                        }

                        if (topDisease.isNotEmpty()) {
                            resultString.append("Confidence: ${"%.2f".format(topConfidence)}%")
                        }
                    }

                }

                withContext(Dispatchers.Main) {
                    binding.diseaseResultText.text = resultString.toString()
                    binding.diseaseResultText.textSize = 16f // Adjust text size as needed
                    binding.learnmore.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e("Prediction", "Error calling API", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity4,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }



//    // Helper to get asset file path
//    private fun assetFilePath(assetName: String): String {
//        val file = File(filesDir, assetName)
//        if (file.exists() && file.length() > 0) return file.absolutePath
//
//        assets.open(assetName).use { inputStream ->
//            file.outputStream().use { outputStream ->
//                inputStream.copyTo(outputStream)
//            }
//        }
//        return file.absolutePath
//    }
}
