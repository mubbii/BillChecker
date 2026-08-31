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

        const val LESCO_CHECKBILL_URL = "https://bill.pitc.com.pk/lescobill"
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
                if (mode == MODE_CHECK && !number.isNullOrBlank() && !autofillAttempted) {
                    autofillAttempted = true
                    when (company) {
                        Company.LESCO -> injectLescoAutofill(number!!, secondaryId ?: "")
                        Company.SNGPL -> injectSngplAutofill(number!!)
                    }
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

    /**
     * bill.pitc.com.pk/lescobill: a single #searchTextBox field (defaults to
     * "Reference No" mode, already selected), a #ruCodeTextBox suffix
     * dropdown ("U" = empty value, "R" = "R"), and a #btnSearch submit
     * button - no captcha, one page, done.
     *
     * refValue format: RefNo or RefNo-Suffix (suffix optional, defaults to U)
     * e.g. "1234567890" or "1234567890-R"
     */
    private fun injectLescoAutofill(refValue: String, custId: String) {
        val parts = refValue.split("-").map { it.trim() }
        val refNo = parts[0]
        val suffix = if (parts.size > 1 && parts[1].isNotBlank()) parts[1] else "U"

        if (refNo.isBlank()) {
            Toast.makeText(this, "Reference number is empty", Toast.LENGTH_LONG).show()
            return
        }

        val refNoJs = jsString(refNo)
        val suffixIsR = suffix.equals("R", ignoreCase = true)

        val js = """
            (function() {
                try {
                    var input = document.getElementById('searchTextBox');
                    var select = document.getElementById('ruCodeTextBox');
                    var btn = document.getElementById('btnSearch');
                    if (!input || !btn) {
                        return 'FIELDS_MISSING input=' + !!input + ' btn=' + !!btn;
                    }
                    input.value = $refNoJs;
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                    input.dispatchEvent(new Event('change', { bubbles: true }));

                    if (select) {
                        select.value = $suffixIsR ? 'R' : '';
                        select.dispatchEvent(new Event('change', { bubbles: true }));
                    }

                    btn.click();
                    return 'SUBMITTED';
                } catch (e) {
                    return 'ERROR: ' + e.message;
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(js) { result ->
            val clean = result?.trim('"') ?: "null"
            if (!clean.contains("SUBMITTED")) {
                Toast.makeText(this, clean, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * sngpl-bill.pk: a single #gbConsumer input and a #gbBtn button that
     * calls the page's own window.gbFetchBill() function - no captcha,
     * so this fully completes the check with no manual step left over.
     */
    private fun injectSngplAutofill(consumerNumber: String) {
        val value = jsString(consumerNumber)

        val js = """
            (function() {
                try {
                    var input = document.getElementById('gbConsumer');
                    if (!input) return 'INPUT_NOT_FOUND';
                    input.value = $value;
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                    input.dispatchEvent(new Event('change', { bubbles: true }));
                    if (typeof window.gbFetchBill === 'function') {
                        window.gbFetchBill();
                        return 'SUBMITTED';
                    }
                    var btn = document.getElementById('gbBtn');
                    if (btn) {
                        btn.click();
                        return 'SUBMITTED_VIA_BUTTON';
                    }
                    return 'BUTTON_NOT_FOUND';
                } catch (e) {
                    return 'ERROR: ' + e.message;
                }
            })();
        """.trimIndent()

        webView.evaluateJavascript(js) { result ->
            val clean = result?.trim('"') ?: "null"
            if (!clean.contains("SUBMITTED")) {
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
