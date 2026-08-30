package com.ahsan.billchecker

import org.json.JSONObject

data class BillEntry(
    val id: String,
    val label: String,
    val company: Company,
    val number: String,        // Reference number, 4 dash-separated parts for LESCO
    val secondaryId: String = "" // Customer ID (LESCO) - kept separate on purpose
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("label", label)
        put("company", company.name)
        put("number", number)
        put("secondaryId", secondaryId)
    }

    companion object {
        fun fromJson(o: JSONObject): BillEntry = BillEntry(
            id = o.getString("id"),
            label = o.getString("label"),
            company = Company.fromName(o.getString("company")),
            number = o.getString("number"),
            secondaryId = o.optString("secondaryId", "")
        )
    }
}
