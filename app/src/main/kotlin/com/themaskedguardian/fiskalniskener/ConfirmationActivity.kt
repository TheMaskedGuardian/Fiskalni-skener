package com.themaskedguardian.fiskalniskener

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ConfirmationActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var scrollContent: ScrollView
    private lateinit var tvCompany: TextView
    private lateinit var tvPib: TextView
    private lateinit var tvAddress: TextView
    private lateinit var itemsContainer: LinearLayout
    private lateinit var tvTotal: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvInvoiceNumber: TextView
    private lateinit var btnCashew: Button
    private lateinit var btnScanAgain: Button

    private var receiptData: ReceiptData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        // Toolbar sa back dugmetom
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        progressBar      = findViewById(R.id.progress_bar)
        scrollContent    = findViewById(R.id.layout_content)
        tvCompany        = findViewById(R.id.tv_company)
        tvPib            = findViewById(R.id.tv_pib)
        tvAddress        = findViewById(R.id.tv_address)
        itemsContainer   = findViewById(R.id.tv_items_container)
        tvTotal          = findViewById(R.id.tv_total)
        tvDate           = findViewById(R.id.tv_date)
        tvInvoiceNumber  = findViewById(R.id.tv_invoice_number)
        btnCashew        = findViewById(R.id.btn_cashew)
        btnScanAgain     = findViewById(R.id.btn_scan_again)

        val url = intent.getStringExtra(EXTRA_URL) ?: run {
            Toast.makeText(this, "Greška: URL nije pronađen", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        btnCashew.setOnClickListener {
            val data = receiptData ?: return@setOnClickListener
            try {
                CashewLauncher.launch(this, data)
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }

        btnScanAgain.setOnClickListener { finish() }

        fetchReceipt(url)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun fetchReceipt(url: String) {
        progressBar.visibility  = View.VISIBLE
        scrollContent.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val data = ReceiptFetcher.fetch(url)
                receiptData = data
                displayReceipt(data)
                progressBar.visibility   = View.GONE
                scrollContent.visibility = View.VISIBLE
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(
                    this@ConfirmationActivity,
                    "Greška pri učitavanju: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    private fun displayReceipt(data: ReceiptData) {
        tvCompany.text = data.company
        tvPib.text     = "PIB: ${data.tin}"

        val addrParts = listOf(data.address.trim(), data.city.trim()).filter { it.isNotEmpty() }
        tvAddress.text = addrParts.joinToString(", ").ifEmpty { "Adresa nije dostupna" }

        // Stavke
        itemsContainer.removeAllViews()
        for (item in data.items) {
            val qty = if (item.quantity == kotlin.math.floor(item.quantity))
                item.quantity.toInt().toString()
            else
                String.format("%.2f", item.quantity)
            val price = String.format("%.2f", item.unitPrice).replace('.', ',')
            val total = String.format("%.2f", item.total).replace('.', ',')

            val tv = TextView(this)
            tv.text      = "• ${item.name}\n   ${qty} × ${price} = ${total} RSD"
            tv.textSize  = 14f
            tv.setTextColor(getColor(R.color.text_primary))
            tv.setPadding(0, 8, 0, 8)
            itemsContainer.addView(tv)
        }

        tvTotal.text          = "Укупно: ${data.formatAmount()} RSD"
        tvDate.text           = "Датум: ${data.formatDate()}"
        tvInvoiceNumber.text  = "Рачун: ${data.invoiceNumber}"
    }
}
