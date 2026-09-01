package com.ahsan.billchecker

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Add more entries here later - they'll automatically appear as a new
    // card in the same grid, styled to match.
    private val entries: List<Pair<String, () -> Unit>> by lazy {
        listOf(
            getString(R.string.app_name) to {
                startActivity(Intent(this, ChooseModeActivity::class.java))
            },
            getString(R.string.manage_saved_numbers) to {
                startActivity(Intent(this, ManageBillsActivity::class.java))
            },
            getString(R.string.emergency_numbers) to {
                startActivity(Intent(this, EmergencyNumbersActivity::class.java))
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val grid = findViewById<GridLayout>(R.id.buttonsGrid)
        for ((label, action) in entries) {
            grid.addView(buildCard(label, action))
        }
    }

    private fun buildCard(label: String, onClick: () -> Unit): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#EEF3FA"))
            setPadding(dp(12), dp(16), dp(12), dp(16))
            isClickable = true
            isFocusable = true

            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(130)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(6), dp(6), dp(6), dp(6))
            }

            setOnClickListener { onClick() }
        }

        val labelView = TextView(this).apply {
            text = label
            textSize = 15f
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.parseColor("#1565C0"))
        }

        card.addView(labelView)
        return card
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
