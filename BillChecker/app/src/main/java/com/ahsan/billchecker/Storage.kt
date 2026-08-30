package com.ahsan.billchecker

import android.content.Context
import org.json.JSONArray
import java.util.UUID

/**
 * Everything lives in plain SharedPreferences on-device. Nothing is ever
 * sent anywhere by this app itself - the WebView talks to the utility's
 * own site directly, same as opening it in a browser.
 */
object Storage {
    private const val PREFS = "bill_checker_prefs"
    private const val KEY_BILLS = "bills"
    private const val KEY_URL_PREFIX = "company_url_"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getBills(ctx: Context): List<BillEntry> {
        val raw = prefs(ctx).getString(KEY_BILLS, "[]") ?: "[]"
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { BillEntry.fromJson(arr.getJSONObject(it)) }
    }

    private fun saveBills(ctx: Context, bills: List<BillEntry>) {
        val arr = JSONArray()
        bills.forEach { arr.put(it.toJson()) }
        prefs(ctx).edit().putString(KEY_BILLS, arr.toString()).apply()
    }

    fun addBill(ctx: Context, label: String, company: Company, number: String, secondaryId: String = "") {
        val bills = getBills(ctx).toMutableList()
        bills.add(BillEntry(UUID.randomUUID().toString(), label, company, number, secondaryId))
        saveBills(ctx, bills)
    }

    fun deleteBill(ctx: Context, id: String) {
        saveBills(ctx, getBills(ctx).filterNot { it.id == id })
    }

    // Per-company saved "real" bill-check page URL, set once via Setup mode.
    fun getCompanyUrl(ctx: Context, company: Company): String {
        return prefs(ctx).getString(KEY_URL_PREFIX + company.name, null) ?: company.defaultUrl
    }

    fun hasCustomCompanyUrl(ctx: Context, company: Company): Boolean {
        return prefs(ctx).getString(KEY_URL_PREFIX + company.name, null) != null
    }

    fun setCompanyUrl(ctx: Context, company: Company, url: String) {
        prefs(ctx).edit().putString(KEY_URL_PREFIX + company.name, url).apply()
    }
}
