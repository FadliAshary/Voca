<?php
header('Content-Type: application/json');
require_once 'config.php';

$name  = isset($_POST['name']) ? htmlspecialchars($_POST['name']) : '';
$email = isset($_POST['email']) ? htmlspecialchars($_POST['email']) : '';
$pass  = isset($_POST['password']) ? $_POST['password'] : '';

if (empty($name) || empty($email) || empty($pass)) {
    echo json_encode(["success" => false, "message" => "Data tidak lengkap"]);
    exit;
}

// Cek email duplikat
$check = $conn->prepare("SELECT id FROM users WHERE email = ?");
$check->bind_param("s", $email);
$check->execute();
if ($check->get_result()->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Email sudah terdaftar"]);
    exit;
}

// Hash password
$hashed = password_hash($pass, PASSWORD_DEFAULT);

// Insert user
$stmt = $conn->prepare("INSERT INTO users (name, email, password) VALUES (?, ?, ?)");
$stmt->bind_param("sss", $name, $email, $hashed);

if ($stmt->execute()) {
    echo json_encode([
        "success" => true,
        "message" => "Registrasi berhasil",
        "id" => $conn->insert_id,
        "name" => $name
    ]);
} else {
    echo json_encode(["success" => false, "message" => "Gagal menyimpan data: " . $conn->error]);
}
?>