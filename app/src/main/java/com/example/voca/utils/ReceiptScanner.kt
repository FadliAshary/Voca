package com.example.voca.utils

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ReceiptScanner {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun scanReceipt(bitmap: Bitmap, onSuccess: (String, Double?) -> Unit, onFailure: (Exception) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text
                val amount = extractAmount(fullText)
                onSuccess(fullText, amount)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    /**
     * Sederhana: Mencari angka terbesar di bagian bawah atau angka setelah kata "Total"
     */
    private fun extractAmount(text: String): Double? {
        val lines = text.split("\n")
        var maxAmount = 0.0
        
        // Regex untuk mencari pola harga (contoh: 50.000 atau 50000 atau 50,000.00)
        val priceRegex = Regex("""([\d]{1,3}(?:[.,]\d{3})*(?:[.,]\d{2})?)""")
        
        // Coba cari baris yang mengandung kata kunci total
        val totalKeywords = listOf("total", "jumlah", "amount", "netto", "bayar", "grand")
        
        for (line in lines) {
            val lowerLine = line.lowercase()
            if (totalKeywords.any { lowerLine.contains(it) }) {
                val matches = priceRegex.findAll(line)
                matches.lastOrNull()?.let {
                    val value = cleanPriceString(it.value)
                    if (value > 0) return value
                }
            }
        }
        
        // Jika tidak ada kata kunci, ambil angka terbesar yang masuk akal (biasanya total ada di paling bawah)
        for (line in lines) {
            val matches = priceRegex.findAll(line)
            for (match in matches) {
                val value = cleanPriceString(match.value)
                if (value > maxAmount) maxAmount = value
            }
        }
        
        return if (maxAmount > 0) maxAmount else null
    }

    private fun cleanPriceString(priceStr: String): Double {
        // Hapus karakter non-digit kecuali pemisah desimal/ribuan
        // Jika format Indonesia: 50.000,00 -> kita ganti . dengan kosong dan , dengan .
        // Kita asumsikan format umum struk di Indo
        var cleaned = priceStr.replace(Regex("""[^0-9,.]"""), "")
        
        if (cleaned.contains(",") && cleaned.contains(".")) {
            // Campuran . dan , (contoh 1.234,56)
            cleaned = cleaned.replace(".", "").replace(",", ".")
        } else if (cleaned.contains(",")) {
            // Hanya , (contoh 50000,00 atau 50,000)
            // Cek apakah , adalah ribuan atau desimal
            val parts = cleaned.split(",")
            if (parts.last().length == 2) {
                cleaned = cleaned.replace(",", ".") // desimal
            } else {
                cleaned = cleaned.replace(",", "") // ribuan
            }
        } else if (cleaned.contains(".")) {
            // Hanya . (contoh 50.000 atau 50000.00)
            val parts = cleaned.split(".")
            if (parts.last().length == 2) {
                // Desimal (probaby)
            } else {
                cleaned = cleaned.replace(".", "") // ribuan
            }
        }
        
        return cleaned.toDoubleOrNull() ?: 0.0
    }
}
