@echo off
REM Script untuk setup XAMPP dan database Voca App

echo.
echo ========================================
echo  Voca App - XAMPP Setup Script
echo ========================================
echo.

REM Tentukan path
set SOURCE_PATH=C:\Users\fadli\AndroidStudioProjects\Voca2\server
set XAMPP_PATH=C:\xampp\htdocs\voca_db

REM Cek apakah XAMPP sudah ada
if not exist "C:\xampp\htdocs" (
    echo [ERROR] XAMPP tidak ditemukan di C:\xampp\
    echo Pastikan XAMPP sudah diinstall!
    pause
    exit /b 1
)

REM Buat folder voca_db jika belum ada
echo [INFO] Membuat folder %XAMPP_PATH%...
if not exist "%XAMPP_PATH%" (
    mkdir "%XAMPP_PATH%"
    echo [OK] Folder berhasil dibuat
) else (
    echo [OK] Folder sudah ada
)

REM Copy file PHP
echo.
echo [INFO] Menyalin file PHP...
copy "%SOURCE_PATH%\register.php" "%XAMPP_PATH%\register.php" /Y >nul
copy "%SOURCE_PATH%\login.php" "%XAMPP_PATH%\login.php" /Y >nul
copy "%SOURCE_PATH%\add_transaction.php" "%XAMPP_PATH%\add_transaction.php" /Y >nul
copy "%SOURCE_PATH%\config.php" "%XAMPP_PATH%\config.php" /Y >nul
copy "%SOURCE_PATH%\install_db.php" "%XAMPP_PATH%\install_db.php" /Y >nul
copy "%SOURCE_PATH%\voca_db.sql" "%XAMPP_PATH%\voca_db.sql" /Y >nul

if %ERRORLEVEL% EQU 0 (
    echo [OK] Semua file berhasil disalin
) else (
    echo [ERROR] Gagal menyalin file!
    pause
    exit /b 1
)

echo.
echo ========================================
echo  Setup Selesai!
echo ========================================
echo.
echo Langkah selanjutnya:
echo.
echo 1. Buka XAMPP Control Panel
echo    - Jalankan Apache dan MySQL
echo.
echo 2. Setup Database (pilih salah satu):
echo
echo    Opsi A: Buka di browser (recommended)
echo    - http://localhost/voca_db/install_db.php
echo    - Script akan membuat database otomatis
echo.
echo    Opsi B: Import via phpMyAdmin
echo    - Buka http://localhost/phpmyadmin
echo    - Create new database: "voca_db"
echo    - Import file: %XAMPP_PATH%\voca_db.sql
echo.
echo 3. Test API (buka PowerShell dan jalankan):
echo.
echo    Test Register:
echo    Invoke-WebRequest -Uri "http://localhost/voca_db/register.php" ^
echo      -Method POST ^
echo      -Body "name=Test&email=test@example.com&password=password123" ^
echo      -ContentType "application/x-www-form-urlencoded"
echo.
echo    Test Login:
echo    Invoke-WebRequest -Uri "http://localhost/voca_db/login.php" ^
echo      -Method POST ^
echo      -Body "email=test@example.com&password=password123" ^
echo      -ContentType "application/x-www-form-urlencoded"
echo.
echo 4. Jalankan aplikasi Android!
echo.
pause

