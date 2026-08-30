package com.ahsan.billchecker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ChooseModeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_mode)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.app_name)

        findViewById<Button>(R.id.buttonSaved).setOnClickListener {
            startActivity(Intent(this, ChooseSavedBillActivity::class.java))
        }
        findViewById<Button>(R.id.buttonManual).setOnClickListener {
            startActivity(Intent(this, ChooseManualCompanyActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
