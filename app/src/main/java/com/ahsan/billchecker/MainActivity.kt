package com.ahsan.billchecker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: BillAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listBills)
        adapter = BillAdapter()
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val entry = adapter.getItem(position)
            val hasSetupPage = Storage.hasCustomCompanyUrl(this, entry.company)
            if (!hasSetupPage) {
                Toast.makeText(
                    this,
                    "Set up the real ${entry.company.label} bill page first (menu \u2192 Setup)",
                    Toast.LENGTH_LONG
                ).show()
            }
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra(WebViewActivity.EXTRA_MODE, WebViewActivity.MODE_CHECK)
                putExtra(WebViewActivity.EXTRA_COMPANY, entry.company.name)
                putExtra(WebViewActivity.EXTRA_NUMBER, entry.number)
                putExtra(WebViewActivity.EXTRA_SECONDARY_ID, entry.secondaryId)
                putExtra(WebViewActivity.EXTRA_LABEL, entry.label)
            }
            startActivity(intent)
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val entry = adapter.getItem(position)
            AlertDialog.Builder(this)
                .setTitle("Delete entry")
                .setMessage("Remove \"${entry.label}\"?")
                .setPositiveButton("Delete") { _, _ ->
                    Storage.deleteBill(this, entry.id)
                    adapter.refresh()
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            startActivity(Intent(this, AddBillActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val company = when (item.itemId) {
            R.id.action_setup_lesco -> Company.LESCO
            R.id.action_setup_sngpl -> Company.SNGPL
            else -> null
        }
        if (company != null) {
            Toast.makeText(
                this,
                "Navigate to the real ${company.label} bill-inquiry page, then tap the save icon",
                Toast.LENGTH_LONG
            ).show()
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra(WebViewActivity.EXTRA_MODE, WebViewActivity.MODE_SETUP)
                putExtra(WebViewActivity.EXTRA_COMPANY, company.name)
                putExtra(WebViewActivity.EXTRA_LABEL, "Setup: ${company.label}")
            }
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class BillAdapter : BaseAdapter() {
        private var bills: List<BillEntry> = Storage.getBills(this@MainActivity)

        fun refresh() {
            bills = Storage.getBills(this@MainActivity)
            notifyDataSetChanged()
        }

        override fun getCount() = bills.size
        override fun getItem(position: Int): BillEntry = bills[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(this@MainActivity)
                .inflate(R.layout.item_bill, parent, false)
            val entry = bills[position]
            view.findViewById<TextView>(R.id.textLabel).text = entry.label
            view.findViewById<TextView>(R.id.textSubtitle).text = "${entry.company.label} \u2022 ${entry.number}"
            return view
        }
    }
}
