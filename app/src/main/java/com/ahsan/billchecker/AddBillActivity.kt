package com.ahsan.billchecker

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddBillActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bill)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val labelEdit = findViewById<EditText>(R.id.editLabel)
        val numberEdit = findViewById<EditText>(R.id.editNumber)
        val companySpinner = findViewById<Spinner>(R.id.spinnerCompany)
        val saveButton = findViewById<Button>(R.id.buttonSave)
        val customerIdLabel = findViewById<TextView>(R.id.labelCustomerId)
        val customerIdEdit = findViewById<EditText>(R.id.editCustomerId)

        val companies = Company.values().map { it.label }
        companySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, companies)

        companySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateFieldsForCompany(Company.values()[position], numberEdit, customerIdLabel, customerIdEdit)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        updateFieldsForCompany(Company.values()[companySpinner.selectedItemPosition], numberEdit, customerIdLabel, customerIdEdit)

        saveButton.setOnClickListener {
            val label = labelEdit.text.toString().trim()
            val number = numberEdit.text.toString().trim()
            val customerId = customerIdEdit.text.toString().trim()
            val company = Company.values()[companySpinner.selectedItemPosition]

            if (number.isEmpty()) {
                numberEdit.error = "Enter the consumer / reference number"
                return@setOnClickListener
            }
            if (company == Company.LESCO) {
                if (number.split("-").size < 4) {
                    numberEdit.error = "Use the 4-part format (BatchNo-SubDiv-RefNo-Suffix), e.g. 14-11551-141155120-U"
                    return@setOnClickListener
                }
                if (customerId.isEmpty()) {
                    customerIdEdit.error = "Enter your Customer ID"
                    return@setOnClickListener
                }
            }
            val finalLabel = if (label.isEmpty()) company.label else label

            Storage.addBill(this, finalLabel, company, number, customerId)
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateFieldsForCompany(
        company: Company,
        numberEdit: EditText,
        customerIdLabel: TextView,
        customerIdEdit: EditText
    ) {
        when (company) {
            Company.LESCO -> {
                numberEdit.hint = "e.g. 14-11551-141155120-U (batch-subdiv-ref-suffix)"
                customerIdLabel.visibility = View.VISIBLE
                customerIdEdit.visibility = View.VISIBLE
            }
            Company.SNGPL -> {
                numberEdit.hint = "As printed on your bill"
                customerIdLabel.visibility = View.GONE
                customerIdEdit.visibility = View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
