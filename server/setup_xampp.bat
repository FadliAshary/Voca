@echo off
set SOURCE=%~dp0
set TARGET=C:\xampp\htdocs\voca_api

echo ========================================
echo  MENGINSTAL SERVER VOCA KE XAMPP
echo ========================================

if not exist "C:\xampp\htdocs" (
    echo [ERROR] Folder XAMPP tidak ditemukan di C:\xampp\
    pause
    exit
)

if not exist "%TARGET%" (
    mkdir "%TARGET%"
    echo [OK] Folder %TARGET% dibuat.
)

echo [INFO] Menyalin file...
copy "%SOURCE%config.php" "%TARGET%\" /Y
copy "%SOURCE%login.php" "%TARGET%\" /Y
copy "%SOURCE%register.php" "%TARGET%\" /Y
copy "%SOURCE%add_transaction.php" "%TARGET%\" /Y
copy "%SOURCE%install_db.php" "%TARGET%\" /Y

echo.
echo ========================================
echo  SELESAI!
echo ========================================
echo Langkah Terakhir:
echo 1. Jalankan Apache dan MySQL di XAMPP.
echo 2. Buka browser ke: http://localhost/voca_api/install_db.php
echo    (Untuk membuat database otomatis)
echo.
pause