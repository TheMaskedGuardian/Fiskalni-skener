package com.themaskedguardian.fiskalniskener

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

object ReceiptFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun fetch(url: String): ReceiptData = withContext(Dispatchers.IO) {
        val html = getHtml(url)
        if (html.isEmpty()) throw Exception("Prazan odgovor sa servera")

        val doc = Jsoup.parse(html)

        val invoiceNumber = Regex("""viewModel\.InvoiceNumber\s*\(\s*['"]([^'"]+)['"]\s*\)""")
            .find(html)?.groupValues?.get(1) ?: ""
        val token = Regex("""viewModel\.Token\s*\(\s*['"]([^'"]+)['"]\s*\)""")
            .find(html)?.groupValues?.get(1) ?: ""

        val tin = doc.select("#tinLabel").text().trim().split(" ").firstOrNull() ?: ""
        val company = doc.select("#shopFullNameLabel").text().trim().ifEmpty { "Nepoznata prodavnica" }
        val address = doc.select("#addressLabel").text().trim()
        val city = doc.select("#administrativeUnitLabel").text().trim()
        
        val rawTotal = doc.select("#totalAmountLabel").text().trim()
        val totalAmount = try {
            rawTotal.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        } catch (e: Exception) { 0.0 }

        val dateTime = doc.select("#sdcDateTimeLabel").text().trim()
        val invoiceNum = doc.select("#invoiceNumberLabel").text().trim()

        if (tin.isEmpty() && company == "Nepoznata prodavnica" && totalAmount == 0.0) {
            throw Exception("Format računa nije prepoznat. Proverite da li je link ispravan.")
        }

        val items = if (invoiceNumber.isNotEmpty() && token.isNotEmpty()) {
            fetchSpecifications(invoiceNumber, token, url)
        } else {
            emptyList()
        }

        ReceiptData(
            tin = tin,
            company = company,
            storeName = company,
            address = address,
            city = city,
            totalAmount = totalAmount,
            dateTime = dateTime,
            invoiceNumber = invoiceNum,
            items = items
        )
    }

    private fun getHtml(url: String): String {
        return try {
            val req = Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
            client.newCall(req).execute().use { r ->
                if (!r.isSuccessful) return ""
                r.body?.string() ?: ""
            }
        } catch (e: Exception) {
            Log.e("ReceiptFetcher", "Greška pri preuzimanju HTML-a", e)
            ""
        }
    }

    private fun fetchSpecifications(invoiceNumber: String, token: String, referer: String): List<ReceiptItem> {
        return try {
            val body = FormBody.Builder()
                .add("invoiceNumber", invoiceNumber)
                .add("token", token)
                .build()

            val req = Request.Builder()
                .url("https://suf.purs.gov.rs/specifications")
                .post(body)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Referer", referer)
                .build()

            client.newCall(req).execute().use { r ->
                if (!r.isSuccessful) return emptyList()
                val responseText = r.body?.string() ?: return emptyList()
                
                val type = object : TypeToken<Map<String, Any>>() {}.type
                val responseMap: Map<String, Any> = gson.fromJson(responseText, type)
                
                val rawItems = responseMap["items"] as? List<Map<String, Any>> ?: emptyList()
                
                rawItems.map { map ->
                    ReceiptItem(
                        name = (map["name"] as? String)?.trim() ?: "Stavka",
                        quantity = (map["quantity"] as? Number)?.toDouble() ?: 0.0,
                        unitPrice = (map["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                        total = (map["total"] as? Number)?.toDouble() ?: 0.0
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("ReceiptFetcher", "Greška u parsiranju specifikacija", e)
            emptyList()
        }
    }
}
