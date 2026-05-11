package com.themaskedguardian.fiskalniskener

import java.util.Locale

data class ReceiptItem(
    val name: String,
    val quantity: Double,
    val unitPrice: Double,
    val total: Double
)

data class ReceiptData(
    val tin: String,
    val company: String,
    val storeName: String,
    val address: String,
    val city: String,
    val totalAmount: Double,
    val dateTime: String,
    val invoiceNumber: String,
    val items: List<ReceiptItem>
) {
    fun sanitize(text: String): String {
        if (text.isEmpty()) return ""
        
        val map = mapOf(
            'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'ђ' to "dj", 'е' to "e", 'ж' to "z", 'з' to "z", 'и' to "i",
            'ј' to "j", 'к' to "k", 'л' to "l", 'љ' to "lj", 'м' to "m", 'н' to "n", 'њ' to "nj", 'о' to "o", 'п' to "p", 'р' to "r",
            'с' to "s", 'т' to "t", 'ћ' to "c", 'у' to "u", 'ф' to "f", 'х' to "h", 'ц' to "c", 'č' to "c", 'џ' to "dz", 'ш' to "s",
            'А' to "A", 'Б' to "B", 'В' to "V", 'Г' to "G", 'Д' to "D", 'Ђ' to "Dj", 'Е' to "E", 'Ж' to "Z", 'З' to "Z", 'И' to "I",
            'Ј' to "J", 'К' to "K", 'Л' to "L", 'Љ' to "Lj", 'М' to "M", 'Н' to "N", 'њ' to "nj", 'О' to "O", 'П' to "P", 'Р' to "R",
            'С' to "S", 'Т' to "T", 'Ћ' to "C", 'У' to "U", 'Ф' to "F", 'Х' to "H", 'Ц' to "C", 'Č' to "C", 'џ' to "dz", 'Š' to "S",
            'š' to "s", 'đ' to "dj", 'č' to "c", 'ć' to "c", 'ž' to "z",
            'Š' to "S", 'Đ' to "Dj", 'Č' to "C", 'Ć' to "C", 'Ž' to "Z",
            '&' to "+",
            '%' to "pct"
        )

        val result = StringBuilder()
        for (char in text) {
            val replacement = map[char]
            if (replacement != null) {
                result.append(replacement)
            } else if (char.code in 32..126) {
                result.append(char)
            }
        }
        return result.toString()
    }

    fun formatDate(): String {
        return try {
            val clean = dateTime.trim()
            if (clean.contains("/")) {
                val parts = clean.split(" ")
                val dateParts = parts[0].split("/")
                val timeParts = parts[1].split(":")
                val isPm = parts.size >= 3 && parts[2].uppercase() == "PM"
                val month = dateParts[0].padStart(2, '0')
                val day = dateParts[1].padStart(2, '0')
                val year = dateParts[2]
                var hour = timeParts[0].toInt()
                if (isPm && hour != 12) hour += 12
                if (!isPm && hour == 12) hour = 0
                "$day.$month.$year. ${hour.toString().padStart(2, '0')}:${timeParts[1]}:${timeParts[2]}"
            } else {
                clean
            }
        } catch (e: Exception) {
            dateTime
        }
    }

    fun formatDateIso(): String {
        return try {
            val clean = dateTime.trim()
            if (clean.contains("/")) {
                val parts = clean.split(" ")
                val dateParts = parts[0].split("/")
                val timeParts = parts[1].split(":")
                val isPm = parts.size >= 3 && parts[2].uppercase() == "PM"
                val y = dateParts[2]
                val m = dateParts[0].padStart(2, '0')
                val d = dateParts[1].padStart(2, '0')
                var hour = timeParts[0].toInt()
                if (isPm && hour != 12) hour += 12
                if (!isPm && hour == 12) hour = 0
                val hh = hour.toString().padStart(2, '0')
                val mm = timeParts[1].padStart(2, '0')
                val ss = timeParts[2].padStart(2, '0')
                "${y}-${m}-${d}T${hh}:${mm}:${ss}"
            } else {
                val parts = clean.split(" ")
                val dateStr = parts[0].trimEnd('.')
                val p = dateStr.split(".")
                val y = p[2]
                val m = p[1].padStart(2, '0')
                val d = p[0].padStart(2, '0')
                val timeStr = parts[1]
                "${y}-${m}-${d}T${timeStr}"
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun toCashewNotes(): String {
        return generateNotes(isForCashew = true)
    }

    fun toFullNotes(): String {
        return generateNotes(isForCashew = false)
    }

    private fun generateNotes(isForCashew: Boolean): String {
        val sb = StringBuilder()
        val separator = "---"
        sb.appendLine("FISKALNI RACUN")
        sb.appendLine(sanitize(company))
        
        // Adresa i grad idu u obe verzije
        if (address.isNotEmpty()) sb.appendLine(sanitize(address))
        if (city.isNotEmpty()) sb.appendLine(sanitize(city))
        
        sb.appendLine("PIB: $tin")
        sb.appendLine(separator)
        
        for (i in items.indices) {
            val item = items[i]
            val qtyStr = if (item.quantity == Math.floor(item.quantity)) 
                item.quantity.toInt().toString() 
            else 
                String.format(Locale.US, "%.2f", item.quantity)
            
            val totalStr = String.format(Locale.US, "%.2f", item.total)
            val priceStr = String.format(Locale.US, "%.2f", item.unitPrice)
            
            // Prefiks: nista za Cashew, emoji za Clipboard
            val prefix = if (isForCashew) "" else "✅ "
            
            sb.appendLine("${prefix}${sanitize(item.name)}")
            sb.appendLine("  ${qtyStr} x $priceStr = $totalStr RSD")
            
            if (i < items.size - 1) {
                sb.appendLine()
            }
        }
        
        sb.appendLine(separator)
        sb.appendLine("UKUPNO: ${String.format(Locale.US, "%.2f", totalAmount)} RSD")
        
        // Datum i broj racuna idu u obe verzije
        sb.appendLine("Datum: ${formatDate()}")
        sb.appendLine("Racun: $invoiceNumber")
        
        return sb.toString()
    }

    fun formatAmount(): String {
        return String.format(Locale("sr", "RS"), "%.2f", totalAmount)
    }
}
