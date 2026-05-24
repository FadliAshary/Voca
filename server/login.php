<?php
header('Content-Type: application/json');
require_once 'config.php';

$email = isset($_POST['email']) ? htmlspecialchars($_POST['email']) : '';
$pass  = isset($_POST['password']) ? $_POST['password'] : '';

if (empty($email) || empty($pass)) {
    echo json_encode(["success" => false, "message" => "Email dan password wajib diisi"]);
    exit;
}

$stmt = $conn->prepare("SELECT id, name, password FROM users WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();
    if (password_verify($pass, $user['password'])) {
        echo json_encode([
            "success" => true,
            "message" => "Login berhasil",
            "id" => $user['id'],
            "name" => $user['name']
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Password salah"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Email tidak terdaftar"]);
}
?>