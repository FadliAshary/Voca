<?php
header('Content-Type: text/plain');

$host = "localhost";
$user = "root";
$pass = "";
$db   = "voca_db";

$conn = new mysqli($host, $user, $pass);

if ($conn->connect_error) {
    die("Koneksi gagal: " . $conn->connect_error);
}

// Buat Database
$conn->query("CREATE DATABASE IF NOT EXISTS $db");
$conn->select_db($db);

// Buat Tabel Users
$tableUsers = "CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)";

if ($conn->query($tableUsers)) {
    echo "Tabel 'users' siap.\n";
} else {
    echo "Gagal membuat tabel users: " . $conn->error . "\n";
}

// Buat Tabel Finance
$tableFinance = "CREATE TABLE IF NOT EXISTS finance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    title VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    type ENUM('income', 'expense') NOT NULL,
    category VARCHAR(100),
    date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)";

if ($conn->query($tableFinance)) {
    echo "Tabel 'finance' siap.\n";
} else {
    echo "Gagal membuat tabel finance: " . $conn->error . "\n";
}

echo "\nSetup Selesai! Database '$db' sudah siap digunakan.";
$conn->close();
?>