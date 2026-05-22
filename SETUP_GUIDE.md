# 📱 SISTEM REGISTRASI & LOGIN - PANDUAN LENGKAP

## ✅ Apa yang sudah saya lakukan?

### 1. **Database Changes** 📊
- ✅ Tambah tabel `users` di `voca_db.sql`
- ✅ Update tabel `finance` dengan kolom `user_id` (foreign key)
- ✅ File: `C:\xampp\htdocs\voca_db\voca_db.sql`

### 2. **PHP Endpoints** 🔧
- ✅ **register.php** - Endpoint untuk registrasi akun baru
  - Validasi email unik
  - Hash password dengan `password_hash()`
  - Response JSON
  
- ✅ **login.php** - Endpoint untuk login
  - Cari user berdasarkan email
  - Verify password dengan `password_verify()`
  - Return user info jika sukses

### 3. **Android Code** 📱
- ✅ **RegisterActivity.kt** - Ubah ke Retrofit API
  - Hapus: `db.addUser()` (SQLite lokal)
  - Tambah: `apiService.register()` (API ke XAMPP)
  
- ✅ **LoginActivity.kt** - Ubah ke Retrofit API
  - Hapus: `db.checkUser()` (SQLite lokal)
  - Tambah: `apiService.login()` (API ke XAMPP)

### 4. **File sudah di XAMPP** 🎯
```
C:\xampp\htdocs\voca_db\
├── register.php
├── login.php
├── add_transaction.php
├── config.php
├── install_db.php
├── voca_db.sql
└── AUTH_SETUP.md
```

---

## 🚀 Cara Setup dan Test

### **Langkah 1: Setup Database (pilih salah satu)**

#### **Opsi A: AUTO (Recommended) - Jalankan di Browser**
```
1. Pastikan XAMPP running (Apache + MySQL)
2. Buka browser: http://localhost/voca_db/install_db.php
3. Tunggu sampai ada response JSON sukses
4. Done! Database sudah siap
```

**Response yang diharapkan:**
```json
{
  "success": true,
  "message": "Database dan tabel berhasil dibuat"
}
```

#### **Opsi B: Manual - phpMyAdmin**
```
1. Buka: http://localhost/phpmyadmin
2. Click "New" untuk buat database baru
3. Nama database: "voca_db"
4. Charset: utf8mb4_unicode_ci
5. Click "Create"
6. Pilih database "voca_db"
7. Tab "Import" → pilih file voca_db.sql
8. Click "Go"
```

---

### **Langkah 2: Test Registrasi dengan Postman atau cURL**

#### **Test di PowerShell:**
```powershell
# Test Registrasi
$body = @{
    name = "John Doe"
    email = "john@example.com"
    password = "password123"
}

$response = Invoke-WebRequest -Uri "http://localhost/voca_db/register.php" `
    -Method POST `
    -Body $body `
    -ContentType "application/x-www-form-urlencoded"

$response.Content | ConvertFrom-Json | ConvertTo-Json
```

**Response Success:**
```json
{
  "success": true,
  "message": "Registrasi berhasil",
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

#### **Test Login:**
```powershell
$body = @{
    email = "john@example.com"
    password = "password123"
}

$response = Invoke-WebRequest -Uri "http://localhost/voca_db/login.php" `
    -Method POST `
    -Body $body `
    -ContentType "application/x-www-form-urlencoded"

$response.Content | ConvertFrom-Json | ConvertTo-Json
```

**Response Success:**
```json
{
  "success": true,
  "message": "Login berhasil",
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com"
}
```

---

### **Langkah 3: Test Aplikasi Android**

1. **Buka Android Studio**
2. **Build & Run aplikasi**
3. **Tes Registrasi:**
   - Buka screen Registrasi
   - Isi: Name, Email, Password
   - Klik "Register"
   - Harus muncul toast "Registrasi Berhasil"
   - Otomatis balik ke Login screen
   
4. **Tes Login:**
   - Isi email dan password yang tadi diregistrasi
   - Klik "Login"
   - Harus masuk ke HomeActivity
   - Jika error, check Logcat di Android Studio

---

## 🔍 Troubleshooting

### **❌ Error: "Koneksi gagal"**
- Pastikan XAMPP running (cek taskbar)
- Pastikan folder `C:\xampp\htdocs\voca_db\` ada
- Cek BASE_URL di `ApiService.kt`:
  ```kotlin
  private const val BASE_URL = "http://192.168.1.7/voca_db/"
  ```
  
  - Jika test di PC yang sama: ganti `192.168.1.7` dengan `localhost`
  - Jika test di emulator Android: ganti dengan `10.0.2.2`
  - Jika test di device fisik: ganti dengan IP PC Anda (ipconfig → IPv4)

### **❌ Error: "Email sudah terdaftar"**
- Cek database sudah ada kolom `UNIQUE` di kolom `email`
- Coba gunakan email baru yang belum pernah diregistrasi

### **❌ Error: "Email tidak ditemukan" saat login**
- Pastikan sudah register terlebih dahulu
- Cek email yang dipakai sama dengan saat register (case-sensitive)

### **❌ Database tidak ditemukan**
```sql
-- Check di phpMyAdmin apakah database ada:
SELECT * FROM voca_db.users;

-- Jika tidak ada, jalankan install_db.php
```

---

## 📊 Database Schema

### Tabel `users`
```sql
CREATE TABLE users (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Tabel `finance` (Updated)
```sql
CREATE TABLE finance (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id INT UNSIGNED NOT NULL,
  title VARCHAR(255),
  amount DECIMAL(15,2),
  type ENUM('income', 'expense'),
  category VARCHAR(100),
  date DATE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## 🔐 Keamanan

✅ **Sudah diterapkan:**
- Password di-hash dengan `password_hash()` (bcrypt)
- Email unique constraint
- Prepared statements (SQL Injection protection)
- Input validation & sanitization

⚠️ **Untuk Produksi:**
- Gunakan HTTPS
- Implementasi JWT atau session token
- Rate limiting
- CORS configuration
- Input validation lebih ketat
- Error logging

---

## 📝 Summary File yang Dibuat/Diubah

| File | Status | Lokasi |
|------|--------|--------|
| `voca_db.sql` | ✏️ Updated | `/server/` & XAMPP |
| `register.php` | ✨ New | XAMPP |
| `login.php` | ✨ New | XAMPP |
| `RegisterActivity.kt` | ✏️ Updated | Android Project |
| `LoginActivity.kt` | ✏️ Updated | Android Project |
| `ApiService.kt` | ✓ Sudah ada | Android Project |
| `AUTH_SETUP.md` | ✨ New | `/server/` |

---

## ✨ Next Steps (Optional)

1. **Update AddTransactionActivity.kt** - tambahkan `user_id` saat insert transaksi
   ```kotlin
   apiService.addTransaction(
       userId = session.getUserId(),  // Add this
       title = title,
       amount = amount,
       type = type,
       category = category,
       date = date
   )
   ```

2. **Update add_transaction.php** - terima dan simpan `user_id`
   ```php
   $user_id = $_POST['user_id'] ?? 0;
   INSERT INTO finance (user_id, title, ...) VALUES ($user_id, ...);
   ```

3. **Implementasi JWT Token** - untuk session yang lebih aman

4. **Update SessionManager** - simpan `user_id` tidak hanya email

---

**Status:** ✅ **SIAP DIGUNAKAN**
**Last Updated:** May 18, 2026
**Created by:** GitHub Copilot

---

## 🎉 Enjoy!

Akun Anda sekarang langsung tersimpan ke database XAMPP! 🎊

