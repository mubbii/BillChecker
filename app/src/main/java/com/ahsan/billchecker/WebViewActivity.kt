package com.ahsan.billchecker

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var company: Company
    private var mode: String = "CHECK"
    private var number: String? = null
    private var secondaryId: String? = null

    private var autofillAttempted = false

    companion object {
        const val EXTRA_MODE = "mode"
        const val EXTRA_COMPANY = "company"
        const val EXTRA_NUMBER = "number"
        const val EXTRA_SECONDARY_ID = "secondaryId"
        const val EXTRA_LABEL = "label"
        const val MODE_SETUP = "SETUP"
        const val MODE_CHECK = "CHECK"
        const val MODE_MANUAL = "MANUAL"

        const val LESCO_CHECKBILL_URL = "https://www.lesco.gov.pk/Modules/CustomerBillNC/CheckBill.asp"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_CHECK
        company = Company.fromName(intent.getStringExtra(EXTRA_COMPANY) ?: Company.LESCO.name)
        number = intent.getStringExtra(EXTRA_NUMBER)
        secondaryId = intent.getStringExtra(EXTRA_SECONDARY_ID)
        title = intent.getStringExtra(EXTRA_LABEL) ?: company.label

        webView = findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (mode == MODE_CHECK && company == Company.LESCO && !number.isNullOrBlank() && !autofillAttempted) {
                    autofillAttempted = true
                    injectLescoAutofill(number!!, secondaryId ?: "")
                }
            }
        }

        val startUrl = if ((mode == MODE_CHECK || mode == MODE_MANUAL) && company == Company.LESCO) {
            LESCO_CHECKBILL_URL
        } else {
            Storage.getCompanyUrl(this, company)
        }

        if (mode == MODE_CHECK && !number.isNullOrBlank()) {
            copyToClipboard(number!!)
        }

        webView.loadUrl(startUrl)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun copyToClipboard(text: String) {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("consumer_number", text))
    }

    private fun jsString(s: String): String =
        "'" + s.replace("\\", "\\\\").replace("'", "\\'") + "'"

    private fun injectLescoAutofill(refValue: String, custId: String) {
        val parts = refValue.split("-").map { it.trim() }
        if (parts.size < 4) {
            Toast.makeText(
                this,
                "Reference number needs 4 dash-separated parts: BatchNo-SubDiv-RefNo-Suffix",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val p1 = jsString(parts[0])
        val p2 = jsString(parts[1])
        val p3 = jsString(parts[2])
        val suffix = jsString(parts[3])
        val custIdJs = jsString(custId)

        val js = """
            (function() {
                try {
                    var report = [];

                    var form1 = document.querySelector('form[name="form1"]');
                    if (form1) {
                        var batch = form1.querySelector('input[name="txtBatchNo"]');
                        var sub = form1.querySelector('input[name="txtSubDiv"]');
                        var ref = form1.querySelector('input[name="txtRefNo"]');
                        var select = form1.querySelector('select[name="cmbRU"]');
                        if (batch && sub && ref && select) {
                            batch.value = $p1;
                            sub.value = $p2;
                            ref.value = $p3;
                            [batch, sub, ref].forEach(function(el) {
                                el.dispatchEvent(new Event('input', { bubbles: true }));
                                el.dispatchEvent(new Event('change', { bubbles: true }));
                            });
                            var suffixVal = $suffix;
                            for (var i = 0; i < select.options.length; i++) {
                                if (select.options[i].value === suffixVal) {
                                    select.selectedIndex = i;
                                    break;
                                }
                            }
                            select.dispatchEvent(new Event('change', { bubbles: true }));
                            report.push('form1_filled');
                        } else {
                            report.push('form1_fields_missing');
                        }
                    } else {
                        report.push('form1_not_found');
                    }

                    var form2 = document.querySelector('form[name="form2"]');
                    if (form2) {
                        var custIdField = form2.querySelector('input[name="txtCustID"]');
                        var btn2 = form2.querySelector('input[name="btnViewMenu"]');
                        if (custIdField && btn2) {
                            custIdField.value = $custIdJs;
                            custIdField.dispatchEvent(new Event('input', { bubbles: true }));
                            custIdField.dispatchEvent(new Event('change', { bubbles: true }));
                            report.push('form2_filled');
                            btn2.click();
                            report.push('form2_submitted');
                        } else {
                            report.push('form2_fields_missing');
                        }
                    } else {
                        report.push('form2_not_found');
                    }

                    return report.join(' | ');
                } catch (e) {
                    return 'ERROR: ' + e.message;
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(js) { result ->
            val clean = result?.trim('"') ?: "null"
            if (!clean.contains("form2_submitted")) {
                Toast.makeText(this, clean, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (mode == MODE_SETUP) {
            menuInflater.inflate(R.menu.menu_setup, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_save_page -> {
                val current = webView.url
                if (!current.isNullOrBlank()) {
                    Storage.setCompanyUrl(this, company, current)
                    Toast.makeText(this, "Saved as the ${company.label} bill page", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
