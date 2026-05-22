@echo off
REM Quick Test Script - Registrasi & Login ke XAMPP Voca App
REM Jalankan file ini jika XAMPP sudah running

color 0A
title Voca App - Quick Test
cls

echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║     VOCA APP - XAMPP DATABASE TEST                      ║
echo ║     Registrasi & Login Testing                          ║
echo ╚════════════════════════════════════════════════════════╝
echo.

REM Check if XAMPP is accessible
echo [CHECK] Checking if XAMPP is accessible...
curl -s http://localhost/index.php >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    color 0C
    echo [ERROR] XAMPP tidak accessible!
    echo.
    echo Pastikan:
    echo 1. XAMPP Control Panel sudah dibuka
    echo 2. Apache dan MySQL dalam status "Running"
    echo 3. Coba buka di browser: http://localhost
    echo.
    pause
    exit /b 1
)

color 0A
echo [OK] XAMPP accessible!
echo.

REM Check if voca_db folder exists
echo [CHECK] Checking if voca_db folder exists...
if not exist "C:\xampp\htdocs\voca_db\" (
    color 0C
    echo [ERROR] Folder C:\xampp\htdocs\voca_db\ tidak ada!
    echo.
    echo Solusi:
    echo 1. Copy file dari: C:\Users\fadli\AndroidStudioProjects\Voca2\server\*
    echo 2. Ke folder: C:\xampp\htdocs\voca_db\
    echo 3. Atau jalankan: setup_xampp.bat
    echo.
    pause
    exit /b 1
)

color 0A
echo [OK] Folder voca_db exists!
echo.

REM Test install_db.php
echo ╔════════════════════════════════════════════════════════╗
echo ║ TEST 1: Setup Database                                 ║
echo ╚════════════════════════════════════════════════════════╝
echo.
echo Buka di browser: http://localhost/voca_db/install_db.php
echo.
echo Response yang diharapkan:
echo {
echo   "success": true,
echo   "message": "Database dan tabel berhasil dibuat"
echo }
echo.
pause

cls

REM Test Registrasi
color 0A
echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║ TEST 2: Registrasi User                                ║
echo ╚════════════════════════════════════════════════════════╝
echo.

setlocal enabledelayedexpansion
set /p TESTNAME="Masukkan nama (default: Test User): " || set "TESTNAME=Test User"
set /p TESTEMAIL="Masukkan email (default: test@example.com): " || set "TESTEMAIL=test@example.com"
set /p TESTPASS="Masukkan password (default: password123): " || set "TESTPASS=password123"

echo.
echo [TESTING] Mengirim request registrasi...
echo   Name: !TESTNAME!
echo   Email: !TESTEMAIL!
echo   Password: !TESTPASS!
echo.

powershell -Command ^
  "$body = @{ name='!TESTNAME!'; email='!TESTEMAIL!'; password='!TESTPASS!' }; " ^
  "$r = Invoke-WebRequest -Uri 'http://localhost/voca_db/register.php' -Method POST -Body $body -ContentType 'application/x-www-form-urlencoded'; " ^
  "$r.Content | ConvertFrom-Json | ConvertTo-Json"

echo.
pause

cls

REM Test Login
color 0A
echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║ TEST 3: Login User                                     ║
echo ╚════════════════════════════════════════════════════════╝
echo.
echo [TESTING] Mengirim request login...
echo   Email: !TESTEMAIL!
echo   Password: !TESTPASS!
echo.

powershell -Command ^
  "$body = @{ email='!TESTEMAIL!'; password='!TESTPASS!' }; " ^
  "$r = Invoke-WebRequest -Uri 'http://localhost/voca_db/login.php' -Method POST -Body $body -ContentType 'application/x-www-form-urlencoded'; " ^
  "$r.Content | ConvertFrom-Json | ConvertTo-Json"

echo.
pause

cls

REM Check Database
color 0A
echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║ TEST 4: Check Database (phpMyAdmin)                    ║
echo ╚════════════════════════════════════════════════════════╝
echo.
echo Buka di browser: http://localhost/phpmyadmin
echo.
echo Kemudian:
echo 1. Pilih database "voca_db"
echo 2. Pilih tabel "users"
echo 3. Lihat apakah data user sudah ada
echo.
pause

cls

REM Summary
color 0A
echo.
echo ╔════════════════════════════════════════════════════════╗
echo ║ TESTING SELESAI!                                       ║
echo ╚════════════════════════════════════════════════════════╝
echo.
echo Jika semua test sukses:
echo.
echo ✅ Registrasi: User baru tercatat di database XAMPP
echo ✅ Login: User bisa login dengan email & password
echo ✅ Database: Tabel users dan finance sudah ada
echo.
echo Sekarang Anda bisa:
echo 1. Build & Run aplikasi Android
echo 2. Test registrasi & login di aplikasi
echo 3. Lihat data di phpMyAdmin
echo.
echo Dokumentasi lengkap:
echo - C:\Users\fadli\AndroidStudioProjects\Voca2\SETUP_GUIDE.md
echo - C:\Users\fadli\AndroidStudioProjects\Voca2\CODE_CHANGES.md
echo - C:\xampp\htdocs\voca_db\AUTH_SETUP.md
echo.
pause

