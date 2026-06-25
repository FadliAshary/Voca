package com.example.voca.utils

/**
 * Application Configuration
 * Mudah untuk switch antar mode
 */
object AppConfig {
    var MODE = Mode.PRODUCTION

    // Server URL untuk production
    var SERVER_URL = "http://192.168.1.6/voca_api"

    enum class Mode {
        /**
         * Production: Pakai real server XAMPP
         */
        PRODUCTION,

        MOCK,

        /**
         * Fallback: Coba server dulu, jika fail fallback ke SQLite
         */
        FALLBACK
    }

    fun isMockMode() = MODE == Mode.MOCK
    fun isFallbackMode() = MODE == Mode.FALLBACK
    fun isProductionMode() = MODE == Mode.PRODUCTION
}
