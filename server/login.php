<?php
header('Content-Type: application/json');

// Database connection
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "voca_db";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Koneksi gagal: " . $conn->connect_error]));
}

$email = isset($_POST['email']) ? htmlspecialchars($_POST['email']) : '';
$pass = isset($_POST['password']) ? $_POST['password'] : '';

if (empty($email) || empty($pass)) {
    echo json_encode(["success" => false, "message" => "Email dan password diperlukan"]);
    exit;
}

// Check if user exists
$sql = "SELECT id, name, email, password FROM users WHERE email = ?";
$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();

    // Verify password
    if (password_verify($pass, $user['password'])) {
        echo json_encode([
            "success" => true,
            "message" => "Login berhasil",
            "id" => $user['id'],
            "name" => $user['name'],
            "email" => $user['email']
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Password salah"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Email tidak ditemukan"]);
}

$stmt->close();
$conn->close();
?>

