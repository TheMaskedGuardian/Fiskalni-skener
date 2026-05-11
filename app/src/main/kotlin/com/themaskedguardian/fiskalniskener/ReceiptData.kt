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
    // Pomocna funkcija za pretvaranje svega u cistu latinicu
    fun sanitize(text: String): String {
        var result = text
        val cyrillic = "абвгдђежзијклљмнњопрстуфхцчџшАБВГДЂЕЖЗИЈКЛЉМНЊОПРСТУФХЦЧЏШ".toCharArray()
        val latin = arrayOf("a","b","v","g","d","dj","e","z","z","i","j","k","l","lj","m","n","nj","o","p","r","s","t","u","f","h","c","c","dz","s",
                            "A","B","V","G","D","Dj","E","Z","Z","I","J","K","L","Lj","M","N","Nj","O","P","R","S","T","U","F","H","C","C","Dz","S")
        
        for (i in cyrillic.indices) {
            result = result.replace(cyrillic[i].toString(), latin[i])
        }
        
        return result.replace("ć", "c").replace("č", "c").replace("š", "s").replace("đ", "dj").replace("ž", "z")
                     .replace("Ć", "C").replace("Č", "C").replace("Š", "S").replace("Đ", "Dj").replace("Ž", "Z")
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
                val dateStr = clean.split(" ")[0]
                val p = dateStr.split("/")
                "${p[2]}-${p[0].padStart(2, '0')}-${p[1].padStart(2, '0')}"
            } else {
                val dateStr = clean.split(" ")[0].trimEnd('.')
                val p = dateStr.split(".")
                "${p[2]}-${p[1].padStart(2, '0')}-${p[0].padStart(2, '0')}"
            }
        } catch (e: Exception) {
            ""
        }
    }

    fun formatAmount(): String {
        // Za prikaz u aplikaciji (srpski standard sa zarezom)
        return String.format(Locale("sr", "RS"), "%.2f", totalAmount)
    }

    fun toCashewNotes(): String {
        val sb = StringBuilder()
        sb.appendLine("FISKALNI RACUN")
        sb.appendLine(sanitize(company))
        if (address.isNotEmpty()) sb.appendLine(sanitize(address))
        if (city.isNotEmpty()) sb.appendLine(sanitize(city))
        sb.appendLine("PIB: $tin")
        sb.appendLine("---")
        
        for (item in items) {
            val qtyStr = if (item.quantity == Math.floor(item.quantity)) 
                item.quantity.toInt().toString() 
            else 
                String.format(Locale.US, "%.2f", item.quantity)
            
            val totalStr = String.format(Locale.US, "%.2f", item.total)
            
            val name = sanitize(item.name)
            val shortName = if (name.length > 20) name.substring(0, 17) + ".." else name
            sb.appendLine("$shortName (${qtyStr}x) $totalStr")
        }
        
        sb.appendLine("---")
        sb.appendLine("TOTAL: ${String.format(Locale.US, "%.2f", totalAmount)} RSD")
        return sb.toString()
    }
}
