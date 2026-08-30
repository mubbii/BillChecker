package com.ahsan.billchecker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.buttonBillChecker).setOnClickListener {
            showMainChoiceDialog()
        }
        findViewById<Button>(R.id.buttonManage).setOnClickListener {
            startActivity(Intent(this, ManageBillsActivity::class.java))
        }
    }

    private fun showMainChoiceDialog() {
        val options = arrayOf("Check My Bill (saved number)", "Check Manually (blank form)")
        AlertDialog.Builder(this)
            .setTitle("Bill Checker")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> handleSavedChoice()
                    1 -> handleManualChoice()
                }
            }
            .show()
    }

    private fun handleSavedChoice() {
        val bills = Storage.getBills(this)
        when {
            bills.isEmpty() -> {
                Toast.makeText(this, "No saved number yet \u2014 add one first", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, AddBillActivity::class.java))
            }
            bills.size == 1 -> openCheck(bills[0])
            else -> {
                val labels = bills.map { "${it.label} (${it.company.label})" }.toTypedArray()
                AlertDialog.Builder(this)
                    .setTitle("Which one?")
                    .setItems(labels) { _, which -> openCheck(bills[which]) }
                    .show()
            }
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

    private fun handleManualChoice() {
        val companies = Company.values().map { it.label }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Which company?")
            .setItems(companies) { _, which ->
                val company = Company.values()[which]
                val intent = Intent(this, WebViewActivity::class.java).apply {
                    putExtra(WebViewActivity.EXTRA_MODE, WebViewActivity.MODE_MANUAL)
                    putExtra(WebViewActivity.EXTRA_COMPANY, company.name)
                    putExtra(WebViewActivity.EXTRA_LABEL, "${company.label} (manual)")
                }
                startActivity(intent)
            }
            .show()
    }
}
