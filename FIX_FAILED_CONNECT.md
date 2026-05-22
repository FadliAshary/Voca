# 🔥 QUICK FIX: Error "Failed to Connect"

## ✅ SUDAH DIUPDATE!

Saya telah memperbaiki masalah Anda:

**File:** `ApiService.kt`  
**Line 51:** `BASE_URL = "http://10.0.2.2/voca_db/"`  
✅ **Sudah diubah dari** `http://192.168.1.7/voca_db/`

---

## 🚀 Cara Mengatasi Error

### **Step 1: Build Ulang Aplikasi**
```
Android Studio:
1. Menu → Build → Clean Project
2. Menu → Build → Rebuild Project
3. Menu → Run → Run 'app'
4. Tunggu sampai APK selesai build & deploy ke emulator
```

### **Step 2: Test di Emulator**
```
1. Buka aplikasi Voca
2. Pergi ke Register screen
3. Isi data:
   - Name: "Test"
   - Email: "test@example.com"
   - Password: "password123"
4. Klik "Register"
```

### **Step 3: Hasil yang Diharapkan**
```
✅ Toast muncul: "Registrasi Berhasil"
✅ Auto-kembali ke Login screen
✅ Data user tersimpan di database XAMPP
```

---

## ⚠️ Jika Masih Error

### **Penyebab Umum:**

#### **1. XAMPP Tidak Running**
```
Solusi:
1. Buka XAMPP Control Panel
2. Klik "Start" Apache (harus HIJAU)
3. Klik "Start" MySQL (harus HIJAU)
```

#### **2. Database Belum Setup**
```
Solusi:
1. Buka browser: http://localhost/voca_db/install_db.php
2. Tunggu sampai response: {"success": true, ...}
3. Jika error, baca TROUBLESHOOTING_CONNECTION.md
```

#### **3. Emulator Tidak Bisa Connect ke PC**
```
Solusi:
Emulator standard Android Studio (bawaan) harus pakai 10.0.2.2
✅ Sudah benar di ApiService.kt!

Jika pakai emulator lain (BlueStacks, Genymotion):
→ Ubah 10.0.2.2 dengan IP PC Anda
→ Cari IP: ipconfig di PowerShell
→ Contoh: 192.168.x.x atau 10.0.x.x
```

---

## 📝 Checklist

- [x] ApiService.kt BASE_URL sudah diupdate ✅
- [ ] XAMPP running? (Apache HIJAU, MySQL HIJAU)
- [ ] Database sudah setup? (buka install_db.php)
- [ ] Build & Run ulang aplikasi?
- [ ] Test di emulator?

**Jika semua ✅, error harus solved!**

---

## 🆘 Debugging Tips

Jika masih error, buka:

1. **Logcat di Android Studio:**
   - Bottom panel → Logcat
   - Filter: "RegisterActivity" atau "ApiService"
   - Lihat error message lengkap

2. **Test endpoint di browser:**
   ```
   http://localhost/voca_db/register.php
   (harus show error atau response JSON, bukan blank page)
   ```

3. **Baca dokumentasi lengkap:**
   - `TROUBLESHOOTING_CONNECTION.md`
   - `SETUP_GUIDE.md`

---

**Updated:** May 18, 2026  
**Status:** ✅ READY TO TEST

Coba langkah di atas dan beritahu saya jika masih ada masalah! 🎉

