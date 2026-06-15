package com.example.voca.utils

import java.text.NumberFormat
import java.util.*

object CurrencyUtils {
    // Kurs tetap sesuai permintaan (1 USD = 17,700 IDR)
    private const val RATE_USD = 17700.0
    private const val RATE_EUR = 18500.0
    private const val RATE_JPY = 115.0

    /**
     * Mengkonversi nilai dari IDR (Base) ke mata uang target
     */
    fun convertFromIDR(amount: Double, targetCurrency: String): Double {
        return when (targetCurrency) {
            "USD" -> amount / RATE_USD
            "EUR" -> amount / RATE_EUR
            "JPY" -> amount / RATE_JPY
            else -> amount // Tetap IDR
        }
    }

    /**
     * Mengkonversi nilai dari mata uang saat ini kembali ke IDR
     */
    fun convertToIDR(amount: Double, currentCurrency: String): Double {
        return when (currentCurrency) {
            "USD" -> amount * RATE_USD
            "EUR" -> amount * RATE_EUR
            "JPY" -> amount * RATE_JPY
            else -> amount
        }
    }

    /**
     * Mendapatkan formatter sesuai mata uang yang dipilih
     */
    fun getFormatter(currencyCode: String): NumberFormat {
        val formatter = when (currencyCode) {
            "USD" -> NumberFormat.getCurrencyInstance(Locale.US)
            "EUR" -> NumberFormat.getCurrencyInstance(Locale.GERMANY)
            "JPY" -> NumberFormat.getCurrencyInstance(Locale.JAPAN)
            else -> NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        }
        return formatter
    }

    /**
     * Format angka dengan simbol mata uang dan spasi khusus untuk Rp
     */
    fun formatCurrency(amount: Double, currencyCode: String): String {
        val converted = convertFromIDR(amount, currencyCode)
        val formatted = getFormatter(currencyCode).format(converted)
        return if (currencyCode == "IDR" || currencyCode.isEmpty()) {
            formatted.replace("Rp", "Rp ")
        } else {
            formatted
        }
    }
}
