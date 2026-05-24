<?php
header('Content-Type: application/json');
require_once 'config.php';

$title    = isset($_POST['title']) ? htmlspecialchars($_POST['title']) : '';
$amount   = isset($_POST['amount']) ? $_POST['amount'] : 0;
$type     = isset($_POST['type']) ? $_POST['type'] : 'expense';
$category = isset($_POST['category']) ? htmlspecialchars($_POST['category']) : 'Lainnya';
$date     = isset($_POST['date']) ? $_POST['date'] : date('Y-m-d');

if (empty($title) || $amount <= 0) {
    echo json_encode(["success" => false, "message" => "Judul dan jumlah nominal wajib diisi"]);
    exit;
}

// Persiapkan statement
$stmt = $conn->prepare("INSERT INTO finance (title, amount, type, category, date) VALUES (?, ?, ?, ?, ?)");
$stmt->bind_param("sdsss", $title, $amount, $type, $category, $date);

if ($stmt->execute()) {
    echo json_encode([
        "success" => true,
        "message" => "Transaksi berhasil disimpan ke database XAMPP",
        "id" => $conn->insert_id
    ]);
} else {
    echo json_encode(["success" => false, "message" => "Gagal menyimpan ke XAMPP: " . $conn->error]);
}
?>