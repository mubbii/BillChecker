package com.ahsan.billchecker

/**
 * The two utility companies this app supports right now.
 * defaultUrl is just a safe landing page (official homepage) to start
 * navigating from during "Setup" - it is NOT guaranteed to be the exact
 * bill-inquiry page, since that path changes and varies by site redesign.
 * The real page you actually use gets saved locally after you set it up once.
 */
enum class Company(val label: String, val defaultUrl: String) {
    LESCO("LESCO (Electricity)", "https://www.lesco.gov.pk/"),
    SNGPL("SNGPL (Sui Gas)", "https://sngpl-bill.pk/");

    companion object {
        fun fromName(name: String): Company =
            values().firstOrNull { it.name == name } ?: LESCO
    }
}
