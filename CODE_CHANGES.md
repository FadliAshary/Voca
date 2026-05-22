# 📋 DOKUMENTASI PERUBAHAN KODE

## Ringkas Perubahan yang Dilakukan

### 1. RegisterActivity.kt
**Dari:** Menyimpan ke SQLite lokal  
**Ke:** Menyimpan ke XAMPP via API

**Perubahan utama:**
```kotlin
// SEBELUM (SQLite)
val res = db.addUser(name, email, pass)

// SESUDAH (API)
val apiService = ApiService.create()
apiService.register(name, email, password)
    .enqueue(object : Callback<Map<String, Any>> { ... })
```

**Import yang ditambah:**
```kotlin
import com.example.voca.api.ApiService
import retrofit2.Callback
import retrofit2.Response
```

**Keuntungan:**
- ✅ Data tersentralisasi di server XAMPP
- ✅ Data bisa diakses dari mana saja
- ✅ Lebih aman (password di-hash server-side)
- ✅ Lebih scalable untuk multi-device

---

### 2. LoginActivity.kt
**Dari:** Cek user di SQLite lokal  
**Ke:** Cek user di XAMPP via API

**Perubahan utama:**
```kotlin
// SEBELUM (SQLite)
if (db.checkUser(email, pass)) {
    session.setLogin(true)
    ...
}

// SESUDAH (API)
val apiService = ApiService.create()
apiService.login(email, password)
    .enqueue(object : Callback<Map<String, Any>> { ... })
```

**Response handling:**
```kotlin
if (body != null && body["success"] == true) {
    session.setLogin(true)
    session.setUserName(body["name"]?.toString() ?: email)
    session.setUserEmail(email)
    ...
}
```

**Keuntungan:**
- ✅ Login verification dari server terpusat
- ✅ Bisa implementasi session management yang lebih baik
- ✅ Support untuk multi-device login

---

### 3. voca_db.sql
**Perubahan:**

#### Tabel Baru: `users`
```sql
CREATE TABLE IF NOT EXISTS `users` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### Tabel Existing: `finance` (Modified)
```sql
-- SEBELUM: Tidak ada user_id
CREATE TABLE IF NOT EXISTS `finance` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  ...
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SESUDAH: Dengan user_id dan foreign key
CREATE TABLE IF NOT EXISTS `finance` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` INT UNSIGNED NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  ...
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**Keuntungan:**
- ✅ Setiap transaksi linked ke user tertentu
- ✅ Bisa filter transaksi per user
- ✅ Cascade delete (hapus user = hapus semua transaksi user)

---

## File PHP Baru

### register.php
```php
POST /register.php
Content-Type: application/x-www-form-urlencoded

name=John Doe&email=john@example.com&password=password123
```

**Fitur:**
- Validasi email unique
- Hash password dengan bcrypt
- Prevent SQL injection (prepared statements)
- Return JSON response

---

### login.php
```php
POST /login.php
Content-Type: application/x-www-form-urlencoded

email=john@example.com&password=password123
```

**Fitur:**
- Validasi email ada di database
- Verify password dengan password_verify()
- Return user info jika login sukses
- Return error message jika gagal

---

## ApiService.kt (Sudah Ada)
```kotlin
interface ApiService {
    @FormUrlEncoded
    @POST("register.php")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") pass: String
    ): Call<Map<String, Any>>

    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("email") email: String,
        @Field("password") pass: String
    ): Call<Map<String, Any>>
}
```

---

## Alur Data (Before vs After)

### SEBELUM (SQLite Local)
```
User Input
    ↓
RegisterActivity
    ↓
DatabaseHelper.addUser() (SQLite)
    ↓
Data tersimpan di internal storage phone
    ↓
❌ Data tidak bisa diakses dari device lain
```

### SESUDAH (API ke XAMPP)
```
User Input
    ↓
RegisterActivity
    ↓
ApiService.register() (Retrofit)
    ↓
register.php (Server)
    ↓
DatabaseHelper (MySQL di XAMPP)
    ↓
✅ Data tersimpan di server XAMPP
✅ Bisa diakses dari device mana saja
✅ Password aman (di-hash server-side)
```

---

## Comparison: SQLite vs API+MySQL

| Aspek | SQLite | API+MySQL |
|-------|--------|-----------|
| **Lokasi Data** | Internal storage device | Server terpusat |
| **Akses Multi-Device** | ❌ Tidak | ✅ Ya |
| **Keamanan Password** | ❌ Plain text | ✅ Hashed (bcrypt) |
| **Skalabilitas** | ❌ Terbatas | ✅ Scalable |
| **Maintenance** | ❌ Sulit | ✅ Mudah |
| **Backup** | ❌ Per device | ✅ Terpusat |
| **Real-time Sync** | ❌ Tidak | ✅ Ya (bisa ditambah) |

---

## Error Handling

### RegisterActivity - Response Handling
```kotlin
override fun onResponse(
    call: retrofit2.Call<Map<String, Any>>,
    response: Response<Map<String, Any>>
) {
    if (response.isSuccessful) {
        val body = response.body()
        if (body != null && body["success"] == true) {
            Toast.makeText(this@RegisterActivity, "Registrasi Berhasil", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            val message = body?.get("message")?.toString() ?: "Registrasi Gagal"
            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}

override fun onFailure(call: retrofit2.Call<Map<String, Any>>, t: Throwable) {
    Toast.makeText(this@RegisterActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
}
```

**Possible Errors:**
- "Email sudah terdaftar" → User harus gunakan email baru
- "Data tidak lengkap" → User harus isi semua field
- "Koneksi gagal" → XAMPP tidak running atau URL salah

---

## Testing Checklist

- [ ] XAMPP running (Apache + MySQL)
- [ ] Database sudah dibuat (run install_db.php)
- [ ] Folder `/voca_db/` ada di `C:\xampp\htdocs\`
- [ ] File PHP (.php) ada di XAMPP folder
- [ ] Android app bisa reach XAMPP (ping test)
- [ ] Test Registrasi dengan Postman/cURL
- [ ] Test Login dengan Postman/cURL
- [ ] Test di Android Emulator/Device
- [ ] Check database dengan phpMyAdmin

---

**Dokumentasi dibuat:** May 18, 2026  
**Status:** Siap untuk production testing

