package com.ahsan.billchecker

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ChooseManualCompanyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_manual_company)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.check_manual)

        val container = findViewById<LinearLayout>(R.id.buttonContainer)

        for ((index, company) in Company.values().withIndex()) {
            val button = Button(this).apply {
                text = company.label
                minWidth = dp(260)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { if (index > 0) topMargin = dp(12) }
                setOnClickListener {
                    val intent = Intent(this@ChooseManualCompanyActivity, WebViewActivity::class.java).apply {
                        putExtra(WebViewActivity.EXTRA_MODE, WebViewActivity.MODE_MANUAL)
                        putExtra(WebViewActivity.EXTRA_COMPANY, company.name)
                        putExtra(WebViewActivity.EXTRA_LABEL, "${company.label} (manual)")
                    }
                    startActivity(intent)
                }
            }
            container.addView(button)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
