# 🔍 DIAGNOSTIC CHECKER - Cek Masalah Connection

Jalankan script PowerShell ini untuk mendeteksi masalah sebenarnya:

```powershell
# DIAGNOSTIC SCRIPT - Connection Troubleshooter
Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  🔍 VOCA APP - CONNECTION DIAGNOSTIC TOOL            ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

# TEST 1: XAMPP Status
Write-Host "TEST 1: XAMPP Status" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
try {
    $r = Invoke-WebRequest -Uri "http://localhost" -TimeoutSec 5 -ErrorAction Stop
    Write-Host "✅ XAMPP RUNNING - Status Code: $($r.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "❌ XAMPP NOT RUNNING OR NOT ACCESSIBLE" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
}

# TEST 2: PHP Folder
Write-Host "`nTEST 2: PHP Files Exist" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
if (Test-Path "C:\xampp\htdocs\voca_db\register.php") {
    Write-Host "✅ register.php FOUND" -ForegroundColor Green
} else {
    Write-Host "❌ register.php NOT FOUND" -ForegroundColor Red
    Write-Host "   Expected: C:\xampp\htdocs\voca_db\register.php" -ForegroundColor Red
}

# TEST 3: Database Setup
Write-Host "`nTEST 3: Database Setup" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
try {
    $r = Invoke-WebRequest -Uri "http://localhost/voca_db/install_db.php" -TimeoutSec 10 -ErrorAction Stop
    Write-Host "✅ install_db.php ACCESSIBLE" -ForegroundColor Green
    Write-Host "   Response: $($r.Content)" -ForegroundColor Green
} catch {
    Write-Host "❌ install_db.php ERROR" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
}

# TEST 4: Register Endpoint
Write-Host "`nTEST 4: Register Endpoint" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
try {
    $body = @{
        name = "DiagnosticTest"
        email = "diagnostic@test.com"
        password = "test12345"
    }
    
    $r = Invoke-WebRequest -Uri "http://localhost/voca_db/register.php" `
        -Method POST `
        -Body $body `
        -ContentType "application/x-www-form-urlencoded" `
        -TimeoutSec 10 `
        -ErrorAction Stop
    
    Write-Host "✅ register.php WORKING" -ForegroundColor Green
    Write-Host "   Response: $($r.Content)" -ForegroundColor Green
} catch {
    Write-Host "❌ register.php ERROR" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
}

# TEST 5: Alternative URLs
Write-Host "`nTEST 5: Test Alternative URLs" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

$urls = @(
    "http://localhost/voca_db/",
    "http://127.0.0.1/voca_db/",
    "http://192.168.1.7/voca_db/",
    "http://10.0.2.2/voca_db/"
)

foreach ($url in $urls) {
    try {
        $r = Invoke-WebRequest -Uri "$($url)register.php" -TimeoutSec 3 -ErrorAction Stop
        Write-Host "✅ $url - SUCCESS" -ForegroundColor Green
    } catch {
        Write-Host "❌ $url - FAILED" -ForegroundColor Red
    }
}

# TEST 6: Network Info
Write-Host "`nTEST 6: Network Information" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
try {
    $ipInfo = ipconfig | Select-String "IPv4"
    Write-Host "Local IPv4 Addresses:" -ForegroundColor Cyan
    Write-Host $ipInfo -ForegroundColor Green
} catch {
    Write-Host "Could not get IP info" -ForegroundColor Red
}

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  END OF DIAGNOSTIC                                    ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

Write-Host "📝 NEXT STEPS:" -ForegroundColor Yellow
Write-Host "1. Cek hasil diagnostic di atas"
Write-Host "2. Lihat mana yang GAGAL (❌)"
Write-Host "3. Ikuti solusi di bawah sesuai dengan yang GAGAL`n" -ForegroundColor Cyan
```

## Solusi Berdasarkan Test Results

### Jika TEST 1 GAGAL (XAMPP Not Running):
```
1. Buka XAMPP Control Panel
2. Klik "Start" Apache (harus hijau)
3. Klik "Start" MySQL (harus hijau)
4. Jalankan diagnostic script lagi
```

### Jika TEST 2 GAGAL (File PHP Not Found):
```powershell
# Copy files ke XAMPP
Copy-Item -Path "C:\Users\fadli\AndroidStudioProjects\Voca2\server\*" `
  -Destination "C:\xampp\htdocs\voca_db\" -Force -Recurse

# Verify
Get-ChildItem "C:\xampp\htdocs\voca_db\*.php"
```

### Jika TEST 3 GAGAL (Database Not Setup):
```
Option A: Otomatis
1. Buka browser: http://localhost/phpmyadmin
2. Jalankan: http://localhost/voca_db/install_db.php

Option B: Manual
1. Buka: http://localhost/phpmyadmin
2. Create Database: "voca_db"
3. Import: C:\xampp\htdocs\voca_db\voca_db.sql
```

### Jika TEST 4 GAGAL (Register Endpoint Error):
```
1. Cek PHP error log: C:\xampp\apache\logs\error.log
2. Cek MySQL user/password di config.php
3. Verify database table structure
```

### Jika TEST 5 Shows Success URL:
```
Gunakan URL yang SUCCESS untuk ApiService.kt
Contoh jika "http://localhost/voca_db/" berhasil:
→ Update BASE_URL di ApiService.kt ke URL itu
```

## Alternative Solutions Jika Semua Gagal

### **OPTION 1: Gunakan MockServer untuk Testing**
Saya akan membuat fake server response untuk testing tanpa XAMPP

### **OPTION 2: Gunakan SQLite Fallback**
Data disimpan ke local database jika server fail

### **OPTION 3: Setup Ulang Dari Nol**
Saya akan setup XAMPP environment baru dengan langkah yang lebih detail

---

**Created:** May 18, 2026

