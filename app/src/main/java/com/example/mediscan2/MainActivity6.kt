package com.example.mediscan2

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.graphics.Typeface
import android.net.Uri
import android.widget.LinearLayout

class MainActivity6 : AppCompatActivity() {
    private lateinit var overviewTab: TextView
    private lateinit var causesTab: TextView
    private lateinit var treatmentsTab: TextView
    private lateinit var preventionTab: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main5)

        // Initialize tab views
        overviewTab = findViewById(R.id.overviewTab)
        causesTab = findViewById(R.id.causesTab)
        treatmentsTab = findViewById(R.id.treatmentsTab)
        preventionTab = findViewById(R.id.preventionTab)

        // Set click listeners for tabs
        overviewTab.setOnClickListener { setActiveTab(overviewTab) }
        causesTab.setOnClickListener { setActiveTab(causesTab) }
        treatmentsTab.setOnClickListener { setActiveTab(treatmentsTab) }
        preventionTab.setOnClickListener { setActiveTab(preventionTab) }

        // Set back button click listener
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            startActivity(Intent(this, MainActivity6::class.java))
        }

        findViewById<TextView>(R.id.learnMoreText).setOnClickListener {
            // Handle click action here
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://trusted-medical-site.com"))
            startActivity(intent)
        }
    }



    private fun setActiveTab(activeTab: TextView) {
        // Reset all tabs
        val tabs = listOf(overviewTab, causesTab, treatmentsTab, preventionTab)
        tabs.forEach { tab ->
            tab.setTextColor(ContextCompat.getColor(this, R.color.gray))
            tab.setTypeface(null, Typeface.NORMAL)
            tab.background = ContextCompat.getDrawable(this, R.drawable.tab_inactive_background)
        }

        // Set active tab
        activeTab.setTextColor(ContextCompat.getColor(this, R.color.white))
        activeTab.setTypeface(null, Typeface.BOLD)
        activeTab.background = ContextCompat.getDrawable(this, R.drawable.tab_active_background)

        // Update content based on selected tab
        updateContent(activeTab.id)
    }

    private fun updateContent(tabId: Int) {
        val contentLayout = findViewById<LinearLayout>(R.id.container)
        contentLayout.removeAllViews()

        when(tabId) {
            R.id.overviewTab -> {
                val overView = layoutInflater.inflate(R.layout.dermatities_overview, contentLayout, false)
                contentLayout.addView(overView)
            }
            R.id.causesTab -> {
                val causesView = layoutInflater.inflate(R.layout.dermatities_causes, contentLayout, false)
                contentLayout.addView(causesView)
            }
            R.id.treatmentsTab -> {
                val treatmentsView = layoutInflater.inflate(R.layout.dermatities_treatments, contentLayout, false)
                contentLayout.addView(treatmentsView)
            }
            R.id.preventionTab -> {
                val preventionView = layoutInflater.inflate(R.layout.dermatities_prevention, contentLayout, false)
                contentLayout.addView(preventionView)
            }
        }
    }
}