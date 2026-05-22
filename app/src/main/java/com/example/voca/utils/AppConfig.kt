# 🎛️ APP CONFIG - Pilih Mode Testing/Production

File ini memudahkan Anda untuk switch antara berbagai mode:

## 📄 Buat file baru: `AppConfig.kt`

**Lokasi:** `app/src/main/java/com/example/voca/utils/AppConfig.kt`

```kotlin
package com.example.voca.utils

/**
 * Application Configuration
 * Mudah untuk switch antar mode
 */
object AppConfig {

    // Mode: PRODUCTION, MOCK, FALLBACK
    var MODE = Mode.PRODUCTION

    // Server URL untuk production
    var SERVER_URL = "http://10.0.2.2/voca_db/"

    enum class Mode {
        /**
         * Production: Pakai real server XAMPP
         */
        PRODUCTION,

        /**
         * Mock: Testing dengan dummy data (tidak perlu XAMPP)
         */
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
```

---

## 🔧 Update `RegisterActivity.kt` untuk support MODE:

Ganti method `registerUser`:

```kotlin
private fun registerUser(name: String, email: String, password: String) {
    when (AppConfig.MODE) {
        AppConfig.Mode.MOCK -> registerWithMock(name, email, password)
        AppConfig.Mode.FALLBACK -> registerWithFallback(name, email, password)
        AppConfig.Mode.PRODUCTION -> registerWithServer(name, email, password)
    }
}

private fun registerWithServer(name: String, email: String, password: String) {
    val apiService = ApiService.create()

    Toast.makeText(this, "Sedang mengirim data ke server...", Toast.LENGTH_SHORT).show()

    apiService.register(name, email, password)
        .enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: retrofit2.Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                android.util.Log.d("RegisterActivity", "Response: ${response.code()} - ${response.body()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body["success"] == true) {
                        Toast.makeText(this@RegisterActivity, "✅ Registrasi Berhasil", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val message = body?.get("message")?.toString() ?: "Registrasi Gagal"
                        Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                val errorMsg = t.message ?: "Unknown error"
                android.util.Log.e("RegisterActivity", "Failure: $errorMsg", t)
                Toast.makeText(this@RegisterActivity, "❌ Connection Failed:\n$errorMsg", Toast.LENGTH_LONG).show()
            }
        })
}

private fun registerWithMock(name: String, email: String, password: String) {
    val apiService = ApiService.createMock()

    Toast.makeText(this, "📱 Mock Mode - Pakai Dummy Data", Toast.LENGTH_SHORT).show()

    apiService.register(name, email, password)
        .enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: retrofit2.Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                val body = response.body()
                if (body != null && body["success"] == true) {
                    Toast.makeText(this@RegisterActivity, "✅ Registrasi (Mock)", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val message = body?.get("message")?.toString() ?: "Registrasi Gagal"
                    Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Mock Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
}

private fun registerWithFallback(name: String, email: String, password: String) {
    val apiService = ApiService.create()
    val db = DatabaseHelper(this)

    apiService.register(name, email, password)
        .enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: retrofit2.Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body["success"] == true) {
                        Toast.makeText(this@RegisterActivity, "✅ Registrasi (Server)", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        fallbackToLocalDB(name, email, password, db)
                    }
                } else {
                    fallbackToLocalDB(name, email, password, db)
                }
            }

            override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
                fallbackToLocalDB(name, email, password, db)
            }
        })
}

private fun fallbackToLocalDB(name: String, email: String, password: String, db: DatabaseHelper) {
    val res = db.addUser(name, email, password)
    if (res > 0) {
        Toast.makeText(
            this,
            "⚠️ Server tidak accessible\n✅ Data disimpan lokal",
            Toast.LENGTH_LONG
        ).show()
        finish()
    } else {
        Toast.makeText(this, "❌ Registrasi Gagal", Toast.LENGTH_SHORT).show()
    }
}
```

---

## 🎮 Cara Menggunakan di Aplikasi

### **Testing dengan Mock Server (Tidak perlu XAMPP):**

Tambahkan di `MainActivity.kt` atau `LandingActivity.kt`:

```kotlin
import com.example.voca.utils.AppConfig

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Pilih mode
    AppConfig.MODE = AppConfig.Mode.MOCK  // Testing tanpa server
    // AppConfig.MODE = AppConfig.Mode.FALLBACK  // Fallback ke SQLite
    // AppConfig.MODE = AppConfig.Mode.PRODUCTION  // Production (default)
}
```

---

## 📋 QUICK MODE REFERENCE

| Mode | Behavior | Kapan Dipakai |
|------|----------|--------------|
| PRODUCTION | Pakai real server XAMPP | Production, semua OK |
| MOCK | Pakai dummy data | Testing tanpa XAMPP |
| FALLBACK | Server + SQLite backup | Safe mode |

---

## 🚀 **RECOMMENDED TESTING FLOW**

```
1. Development:
   AppConfig.MODE = Mode.MOCK
   → Test features tanpa perlu XAMPP running

2. Local Testing:
   AppConfig.MODE = Mode.FALLBACK
   → Test dengan server, fallback ke SQLite jika fail

3. Production:
   AppConfig.MODE = Mode.PRODUCTION
   → Gunakan server real
```

---

**Created:** May 18, 2026

