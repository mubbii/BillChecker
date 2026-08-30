package com.ahsan.billchecker

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ChooseSavedBillActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_saved_bill)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.check_saved)

        rebuildButtons()
    }

    override fun onResume() {
        super.onResume()
        rebuildButtons()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun rebuildButtons() {
        val container = findViewById<LinearLayout>(R.id.buttonContainer)
        container.removeAllViews()

        val bills = Storage.getBills(this)

        if (bills.isEmpty()) {
            val message = TextView(this).apply {
                text = getString(R.string.empty_state)
                setPadding(0, 0, 0, 24)
            }
            container.addView(message)

            val addButton = Button(this).apply {
                text = getString(R.string.add_bill_title)
                minWidth = dp(220)
                setOnClickListener {
                    startActivity(Intent(this@ChooseSavedBillActivity, AddBillActivity::class.java))
                }
            }
            container.addView(addButton)
            return
        }

        for (entry in bills) {
            val button = Button(this).apply {
                text = "${entry.label} (${entry.company.label})"
                minWidth = dp(260)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(12) }
                setOnClickListener { openCheck(entry) }
            }
            container.addView(button)
        }
    }

    private fun openCheck(entry: BillEntry) {
        val intent = Intent(this, WebViewActivity::class.java).apply {
            putExtra(WebViewActivity.EXTRA_MODE, WebViewActivity.MODE_CHECK)
            putExtra(WebViewActivity.EXTRA_COMPANY, entry.company.name)
            putExtra(WebViewActivity.EXTRA_NUMBER, entry.number)
            putExtra(WebViewActivity.EXTRA_SECONDARY_ID, entry.secondaryId)
            putExtra(WebViewActivity.EXTRA_LABEL, entry.label)
        }
        startActivity(intent)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
