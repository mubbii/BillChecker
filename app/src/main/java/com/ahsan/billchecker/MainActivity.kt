package com.ahsan.billchecker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.buttonBillChecker).setOnClickListener {
            startActivity(Intent(this, ChooseModeActivity::class.java))
        }
        findViewById<Button>(R.id.buttonManage).setOnClickListener {
            startActivity(Intent(this, ManageBillsActivity::class.java))
        }
        findViewById<Button>(R.id.buttonEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyNumbersActivity::class.java))
        }
    }
}
