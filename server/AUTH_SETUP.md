# Sistem Registrasi dan Login dengan XAMPP

Panduan lengkap untuk menyimpan data akun pengguna langsung ke database MySQL XAMPP.

## Struktur Database

### Tabel `users`
```sql
CREATE TABLE users (
  id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
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
  FOREIGN KEY (user_id) REFERENCES users(id)
)
```

## File-file yang dibuat

### 1. `register.php`
Endpoint untuk registrasi user baru.

**Request:**
```
POST http://192.168.1.7/voca_db/register.php
Content-Type: application/x-www-form-urlencoded

name=John Doe&email=john@example.com&password=password123
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

**Response Error:**
```json
{
  "success": false,
  "message": "Email sudah terdaftar"
}
```

### 2. `login.php`
Endpoint untuk login user.

**Request:**
```
POST http://192.168.1.7/voca_db/login.php
Content-Type: application/x-www-form-urlencoded

email=john@example.com&password=password123
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

**Response Error:**
```json
{
  "success": false,
  "message": "Email tidak ditemukan"
}
```

## Langkah Setup

### 1. Salin file ke XAMPP
```powershell
Copy-Item -Path "C:\Users\fadli\AndroidStudioProjects\Voca2\server\*" -Destination "C:\xampp\htdocs\voca_db\" -Force -Recurse
```

### 2. Setup Database
```powershell
# Buka phpMyAdmin di browser
# http://localhost/phpmyadmin
# Import file voca_db.sql
# atau
# Jalankan install_db.php di browser
# http://localhost/voca_db/install_db.php
```

### 3. Test Registrasi
```powershell
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

### 4. Test Login
```powershell
$body = @{
    email = "test@example.com"
    password = "password123"
}

$response = Invoke-WebRequest -Uri "http://localhost/voca_db/login.php" `
    -Method POST `
    -Body $body `
    -ContentType "application/x-www-form-urlencoded"

$response.Content | ConvertFrom-Json | ConvertTo-Json
```

## Perubahan pada Android App

### RegisterActivity.kt
- Mengubah dari SQLite lokal (`db.addUser()`) ke API Retrofit
- Mengirim request ke `register.php`
- Handle response JSON dari server

### LoginActivity.kt
- Mengubah dari SQLite lokal (`db.checkUser()`) ke API Retrofit
- Mengirim request ke `login.php`
- Handle response JSON dari server
- Menyimpan user info ke SessionManager

### ApiService.kt (sudah ada)
```kotlin
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
```

## Keamanan

✅ **Fitur Keamanan yang sudah diimplementasikan:**
- Password di-hash menggunakan `password_hash()` (PHP)
- Email unique constraint di database
- Validation data input
- Prepared statements (SQL Injection Prevention)

⚠️ **Untuk Produksi:**
- Gunakan HTTPS, bukan HTTP
- Implementasi JWT token untuk session management
- Rate limiting pada endpoint registrasi/login
- CORS configuration
- Input validation lebih ketat
- Logging dan monitoring

## Testing dengan cURL

### Register
```bash
curl -X POST "http://localhost/voca_db/register.php" \
  -d "name=John Doe" \
  -d "email=john@example.com" \
  -d "password=password123"
```

### Login
```bash
curl -X POST "http://localhost/voca_db/login.php" \
  -d "email=john@example.com" \
  -d "password=password123"
```

## Troubleshooting

### Email sudah terdaftar
- Gunakan email yang belum pernah diregistrasi
- Cek database: `SELECT * FROM users WHERE email = 'xxx@example.com'`

### Password salah saat login
- Pastikan password yang diinput sama persis dengan saat registrasi
- Password case-sensitive

### Koneksi gagal
- Pastikan XAMPP running (Apache + MySQL)
- Cek BASE_URL di `ApiService.kt` sesuai IP komputer Anda
- Untuk emulator Android: gunakan `10.0.2.2` atau IP komputer
- Untuk device fisik: gunakan IP lokal (192.168.x.x)

### Database tidak ditemukan
- Jalankan `install_db.php` untuk membuat database otomatis
- Atau import `voca_db.sql` melalui phpMyAdmin

## Next Steps

1. ✅ Setup database dan file PHP
2. ✅ Update Android activities (Register & Login)
3. ⏳ Test aplikasi end-to-end
4. ⏳ Implementasi session management dengan database
5. ⏳ Ganti API endpoint untuk finance table dengan user_id

---

**Created:** May 18, 2026
**Status:** Ready for integration testing

