# ✅ SOLUSI ERROR "Failed to Connect" - SUDAH DIUPDATE!

## 🎉 Apa yang Saya Lakukan

Saya telah update `ApiService.kt` dengan:
- ✅ BASE_URL diubah ke `http://10.0.2.2/voca_db/` 
- ✅ Ini adalah URL yang benar untuk Android Emulator

---

## 🚀 Langkah Berikutnya

### **1. Build & Run Ulang**
```
Android Studio:
1. Build → Clean Project
2. Build → Rebuild Project
3. Run → Run 'app'
```

### **2. Test Lagi**
```
1. Buka aplikasi
2. Pergi ke Register screen
3. Isi nama, email, password
4. Klik "Register"
5. Jika muncul toast "Registrasi Berhasil" → BERHASIL! ✅
```

---

## 🔍 Jika Masih Error, Cek Ini

### **Problem 1: "Connection Refused"**
```
✓ Pastikan XAMPP Apache & MySQL running (status hijau)
✓ Pastikan folder C:\xampp\htdocs\voca_db\ ada
✓ Pastikan file PHP sudah ada di folder tersebut
```

### **Problem 2: "Network is Unreachable"**
```
✓ Coba test di PC dulu:
  Buka browser: http://localhost/voca_db/register.php
  
✓ Jika di browser tidak muncul apa-apa:
  XAMPP tidak berjalan dengan baik
  Restart XAMPP Control Panel
```

### **Problem 3: "ERR_CLEARTEXT_NOT_PERMITTED"**
```
✓ Pastikan di AndroidManifest.xml sudah ada:
  android:usesCleartextTraffic="true"
  
✓ Sudah ada di line 16 ✅
```

### **Problem 4: Emulator Tidak Bisa Akses PC**
```
✓ Emulator default Android Studio menggunakan IP 10.0.2.2 untuk host
✓ Ini sudah benar di ApiService.kt ✅

Jika pakai emulator lain (BlueStacks, Genymotion, dll):
✓ Ganti 10.0.2.2 dengan IP lokal PC Anda
  Cek IP: ipconfig di PowerShell → IPv4 Address
  Contoh: 192.168.1.5 → BASE_URL = "http://192.168.1.5/voca_db/"
```

---

## 🧪 Test Manual di PowerShell

Jalankan command ini untuk test endpoint:

```powershell
# Test Registrasi
$body = @{
    name = "Test User"
    email = "test@example.com"
    password = "password123"
}

$response = Invoke-WebRequest -Uri "http://localhost/voca_db/register.php" `
    -Method POST `
    -Body $body `
    -ContentType "application/x-www-form-urlencoded"

$response.Content | ConvertFrom-Json | ConvertTo-Json
```

**Output yang diharapkan:**
```json
{
  "success":  true,
  "message":  "Registrasi berhasil",
  "id":  1,
  "name":  "Test User",
  "email":  "test@example.com"
}
```

Jika berhasil di PowerShell tapi gagal di Android, berarti:
- Server OK ✅
- Problem di: URL emulator, network config, atau Android code

---

## 📋 Checklist

- [x] BASE_URL di ApiService.kt sudah diubah ke 10.0.2.2
- [ ] XAMPP Apache running?
- [ ] XAMPP MySQL running?
- [ ] Folder /voca_db/ ada?
- [ ] File PHP ada?
- [ ] Database sudah setup?
- [ ] Test di PowerShell berhasil?
- [ ] Build & Run ulang aplikasi?
- [ ] Test di Emulator?

---

## 🎯 Jika Masih Tidak Berhasil

**Jalankan ini di PowerShell:**

```powershell
Write-Host "=== DIAGNOSTIC TEST ==="
Write-Host ""

# Test 1: XAMPP Accessible
Write-Host "1. Testing XAMPP accessibility..."
try {
    $r = Invoke-WebRequest -Uri "http://localhost/index.php" -TimeoutSec 3
    Write-Host "   ✅ XAMPP is running"
} catch {
    Write-Host "   ❌ XAMPP not accessible"
    Write-Host "      ERROR: $_"
}

# Test 2: voca_db folder
Write-Host ""
Write-Host "2. Checking voca_db folder..."
if (Test-Path "C:\xampp\htdocs\voca_db") {
    Write-Host "   ✅ Folder exists"
    $files = Get-ChildItem "C:\xampp\htdocs\voca_db" -Filter "*.php" | Measure-Object
    Write-Host "   ✅ Found $($files.Count) PHP files"
} else {
    Write-Host "   ❌ Folder NOT found"
}

# Test 3: Database
Write-Host ""
Write-Host "3. Testing database..."
try {
    $r = Invoke-WebRequest -Uri "http://localhost/voca_db/install_db.php" -TimeoutSec 5
    Write-Host "   ✅ install_db.php is accessible"
    Write-Host "   Response: $($r.Content)"
} catch {
    Write-Host "   ❌ install_db.php error"
    Write-Host "      ERROR: $_"
}

# Test 4: Register endpoint
Write-Host ""
Write-Host "4. Testing register endpoint..."
try {
    $body = @{ name="Test"; email="test@test.com"; password="test123" }
    $r = Invoke-WebRequest -Uri "http://localhost/voca_db/register.php" `
        -Method POST -Body $body `
        -ContentType "application/x-www-form-urlencoded" -TimeoutSec 5
    Write-Host "   ✅ register.php is working"
    Write-Host "   Response: $($r.Content)"
} catch {
    Write-Host "   ❌ register.php error"
    Write-Host "      ERROR: $_"
}

Write-Host ""
Write-Host "=== END OF DIAGNOSTIC ==="
```

Jalankan command di atas dan **bagikan hasilnya** jika masih error.

---

## 📞 Support

Jika sudah coba semua tapi masih error, kumpulkan informasi ini:

1. **Logcat Android Studio**
   - Run → Show logcat
   - Filter: "RegisterActivity" atau "ApiService"
   - Copy error message lengkap

2. **XAMPP Error Log**
   - XAMPP folder → logs → apache_error.log
   - Copy error message

3. **Hasil diagnostic script** di atas

Kemudian share data tersebut agar saya bisa diagnose lebih lanjut.

---

**Updated:** May 18, 2026
**Status:** ✅ BASE_URL sudah diupdate untuk Android Emulator

