package com.example.mediscan2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mediscan2.databinding.ActivityMain4Binding
import com.example.mediscan2.ml.Model
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.net.URL

class MainActivity4 : AppCompatActivity() {

    private lateinit var binding: ActivityMain4Binding
    private lateinit var supabaseClient: SupabaseClient
    private lateinit var bitmap: Bitmap

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
        val labels = application.assets.open("label.txt").bufferedReader().readLines()

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f)) // Normalize to 0–1
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val model = Model.newInstance(this)

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 3, 224, 224), DataType.FLOAT32)
        inputFeature0.loadBuffer(tensorImage.buffer)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

        val maxIdx = outputFeature0.indices.maxByOrNull { outputFeature0[it] } ?: 0
        binding.diseaseResultText.text = labels[maxIdx]

        model.close()
    }
}
