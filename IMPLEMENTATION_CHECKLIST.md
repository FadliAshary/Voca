# ✅ IMPLEMENTATION CHECKLIST

Berikut adalah checklist lengkap untuk memastikan semua sudah sesuai:

---

## 📋 Database Setup

- [ ] XAMPP sudah diinstall di `C:\xampp\`
- [ ] XAMPP Control Panel sudah dibuka
- [ ] Apache service running (status hijau)
- [ ] MySQL service running (status hijau)
- [ ] File PHP sudah ada di `C:\xampp\htdocs\voca_db\`
- [ ] Buka `http://localhost/voca_db/install_db.php` di browser
- [ ] Response: `{"success": true, "message": "Database dan tabel berhasil dibuat"}`
- [ ] phpMyAdmin accessible di `http://localhost/phpmyadmin`
- [ ] Database `voca_db` ada di phpMyAdmin
- [ ] Tabel `users` ada dengan kolom: id, name, email, password, created_at
- [ ] Tabel `finance` ada dengan kolom baru: user_id (FK ke users)

---

## 📱 Android Code

- [ ] `RegisterActivity.kt` sudah updated (pakai ApiService)
- [ ] `LoginActivity.kt` sudah updated (pakai ApiService)
- [ ] Semua import sudah ada (ApiService, Callback, Response)
- [ ] `ApiService.kt` sudah punya method `register()` dan `login()`
- [ ] BASE_URL di `ApiService.kt` sesuai:
  - [ ] Emulator: `http://10.0.2.2/voca_db/`
  - [ ] Device fisik: `http://192.168.x.x/voca_db/`
  - [ ] PC lokal: `http://localhost/voca_db/`
- [ ] Permission `INTERNET` ada di `AndroidManifest.xml`
- [ ] AndroidX library yang dibutuhkan sudah installed

---

## 🧪 Testing di PC

Jalankan command berikut di PowerShell:

```powershell
# Test 1: Install Database
Invoke-WebRequest -Uri "http://localhost/voca_db/install_db.php"

# Test 2: Registrasi
$body = @{ name="Test User"; email="test@example.com"; password="password123" }
$r = Invoke-WebRequest -Uri "http://localhost/voca_db/register.php" `
  -Method POST -Body $body -ContentType "application/x-www-form-urlencoded"
$r.Content | ConvertFrom-Json

# Test 3: Login
$body = @{ email="test@example.com"; password="password123" }
$r = Invoke-WebRequest -Uri "http://localhost/voca_db/login.php" `
  -Method POST -Body $body -ContentType "application/x-www-form-urlencoded"
$r.Content | ConvertFrom-Json

# Test 4: Check Database
# Buka http://localhost/phpmyadmin → users table harus ada data
```

Checklist Testing:
- [ ] Install database berhasil (response success: true)
- [ ] Registrasi berhasil (data user tersimpan di database)
- [ ] Email duplicate error jika daftar 2x dengan email sama
- [ ] Login berhasil dengan email & password yang benar
- [ ] Login gagal dengan password salah
- [ ] Data user visible di phpMyAdmin table `users`

---

## 🏃 Testing di Android

### Emulator:
- [ ] Buka Android Studio
- [ ] Create AVD (Android Virtual Device) jika belum ada
- [ ] Run aplikasi di emulator
- [ ] Test Registrasi:
  - [ ] Isi nama, email, password
  - [ ] Klik "Register"
  - [ ] Toast "Registrasi Berhasil" muncul
  - [ ] Auto-back ke Login screen
  - [ ] Cek database: user harus ada di phpMyAdmin
- [ ] Test Login:
  - [ ] Isi email & password tadi
  - [ ] Klik "Login"
  - [ ] Berhasil masuk ke HomeActivity
  - [ ] Toast "Login Berhasil" muncul

### Device Fisik:
- [ ] Device terhubung via USB atau WiFi
- [ ] ADB dapat detect device (adb devices)
- [ ] BASE_URL di ApiService.kt pakai IP PC
- [ ] Device dan PC di network yang sama
- [ ] Test Registrasi & Login (sama seperti emulator)

---

## 📝 Dokumentasi

Pastikan sudah baca file-file ini:

- [ ] `README.md` - Panduan lengkap
- [ ] `SETUP_GUIDE.md` - Setup step-by-step
- [ ] `CODE_CHANGES.md` - Detail perubahan kode
- [ ] `QUICKSTART.md` - Quick reference
- [ ] `AUTH_SETUP.md` (di XAMPP folder) - API docs

---

## 🔧 Troubleshooting

Jika ada error, cek ini:

### Error: "Koneksi gagal"
- [ ] XAMPP running (Apache + MySQL hijau)
- [ ] Folder `C:\xampp\htdocs\voca_db\` ada
- [ ] File PHP ada di folder tsb
- [ ] Coba buka `http://localhost` di browser
- [ ] Cek BASE_URL di ApiService.kt
- [ ] Cek logcat Android Studio untuk error detail

### Error: "Email tidak ditemukan" saat login
- [ ] Pastikan sudah registrasi terlebih dahulu
- [ ] Cek database phpMyAdmin apakah data user ada
- [ ] Cek spelling email (case-sensitive)

### Error: "Database tidak ditemukan"
- [ ] Jalankan `http://localhost/voca_db/install_db.php`
- [ ] Atau import `voca_db.sql` via phpMyAdmin
- [ ] Refresh browser setelah setup

### Error: "cleartext not permitted" (Android 9+)
- [ ] Pastikan `usesCleartextTraffic="true"` di AndroidManifest.xml
- [ ] Atau gunakan HTTPS untuk URL
- [ ] Cek network_security_config.xml

---

## 📊 Verification Database

Pastikan struktur database benar:

### Tabel `users`
```sql
SELECT * FROM voca_db.users;
```
Kolom yang harus ada:
- `id` (INT, PRIMARY KEY, AUTO_INCREMENT)
- `name` (VARCHAR 255, NOT NULL)
- `email` (VARCHAR 255, UNIQUE, NOT NULL)
- `password` (VARCHAR 255, NOT NULL)
- `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)

### Tabel `finance`
```sql
SELECT * FROM voca_db.finance;
```
Kolom yang baru ditambah:
- `user_id` (INT UNSIGNED, NOT NULL, FOREIGN KEY users.id)

---

## 🎯 Success Criteria

✅ Berhasil jika:
1. Database terseup dengan baik
2. Registrasi user baru berhasil menyimpan ke database XAMPP
3. Login user berhasil verifikasi dari database XAMPP
4. Data user visible di phpMyAdmin
5. Aplikasi Android bisa komunikasi dengan XAMPP tanpa error

---

## 📞 Debugging Tips

Jika masih ada error:

1. **Buka Logcat** di Android Studio:
   - Run → Show logcat
   - Filter: "ApiService" atau "RegisterActivity"
   - Lihat error message detail

2. **Buka XAMPP Error Log**:
   - XAMPP → Apache → Logs
   - Cari error message di error.log

3. **Test API Langsung**:
   - Gunakan Postman atau cURL
   - Send request ke endpoint PHP
   - Lihat response JSON

4. **Check Connectivity**:
   - Ping dari emulator ke PC: `adb shell ping 10.0.2.2`
   - Atau test base URL di browser

---

## ✨ Final Check

- [ ] Semua file sudah di tempat yang benar
- [ ] Database sudah setup
- [ ] Android code sudah updated
- [ ] Testing di PC berhasil
- [ ] Testing di Emulator/Device berhasil
- [ ] Dokumentasi sudah dibaca
- [ ] Siap untuk production!

---

**Jika semua checklist sudah ✓ maka sistem Anda 100% siap digunakan!** 🎉

---

**Created:** May 18, 2026  
**Status:** ✅ COMPLETE

