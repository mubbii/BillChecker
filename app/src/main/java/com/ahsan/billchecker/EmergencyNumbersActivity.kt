package com.ahsan.billchecker

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EmergencyNumbersActivity : AppCompatActivity() {

    // name, number to display, number to dial. Sourced from the official
    // Lahore Police Emergency Help Directory:
    // https://lahorepolice.punjab.gov.pk/emergency-help-directory
    // These are Lahore-specific - numbers may differ in other cities.
    private data class Entry(val name: String, val display: String, val dial: String)

    private val sections: List<Pair<String, List<Entry>>> = listOf(
        "Police Helplines" to listOf(
            Entry("Punjab Police", "15", "15"),
            Entry("IGP Complaint Helpline", "1787", "1787"),
            Entry("IGP Complaint (Landline)", "042-99212609", "042-99212609"),
            Entry("Counter Terrorism Dept", "0800-11111", "0800-11111"),
            Entry("Lahore Police Complaint", "8300", "8300"),
            Entry("Lahore Police UAN", "0304-1110911", "0304-1110911")
        ),
        "Inquiries" to listOf(
            Entry("Railway Enquiry", "117", "117"),
            Entry("PIA Enquiry", "114", "114"),
            Entry("Railway Police (add city code)", "1333", "1333")
        ),
        "Emergencies" to listOf(
            Entry("Rescue Service", "1122", "1122"),
            Entry("Fire Brigade", "16", "16")
        ),
        "Medical Facilities" to listOf(
            Entry("Edhi Control Room", "115", "115"),
            Entry("Bomb Disposal Squad", "042-99212111", "042-99212111"),
            Entry("Fatimeed Blood Bank", "042-35863950", "042-35863950"),
            Entry("Punjab Institute of Cardiology", "042-99203051", "042-99203051"),
            Entry("Services Hospital", "042-99203402", "042-99203402"),
            Entry("General Hospital", "042-99264091", "042-99264091"),
            Entry("Mayo Hospital", "042-99211100", "042-99211100"),
            Entry("Jinnah Hospital", "042-99231400", "042-99231400"),
            Entry("Ganga Ram Hospital", "042-99200572", "042-99200572")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_numbers)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.emergency_numbers)

        val container = findViewById<LinearLayout>(R.id.sectionsContainer)

        for ((sectionTitle, entries) in sections) {
            container.addView(buildSectionHeader(sectionTitle))
            container.addView(buildGrid(entries))
        }

        container.addView(buildFootnote())
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun buildSectionHeader(text: String): TextView = TextView(this).apply {
        this.text = text
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        textSize = 16f
        setPadding(0, dp(20), 0, dp(8))
    }

    private fun buildGrid(entries: List<Entry>): GridLayout {
        val columns = 3
        return GridLayout(this).apply {
            columnCount = columns
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            for (entry in entries) {
                addView(buildCard(entry))
            }
        }
    }

    private fun buildCard(entry: Entry): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#EEF3FA"))
            setPadding(dp(8), dp(10), dp(8), dp(10))
            isClickable = true
            isFocusable = true

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(110) // taller than wide - the "vertical rectangle" shape
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(4), dp(4), dp(4), dp(4))
            }
            layoutParams = params

            setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${entry.dial}"))
                startActivity(intent)
            }
        }

        val nameView = TextView(this).apply {
            text = entry.name
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#333333"))
        }

        val numberView = TextView(this).apply {
            text = entry.display
            textSize = 15f
            gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#1565C0"))
            setPadding(0, dp(6), 0, 0)
        }

        card.addView(nameView)
        card.addView(numberView)
        return card
    }

    private fun buildFootnote(): TextView = TextView(this).apply {
        text = getString(R.string.emergency_numbers_footnote)
        textSize = 12f
        setTextColor(Color.parseColor("#777777"))
        setPadding(0, dp(24), 0, dp(8))
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()
}
