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

$name = isset($_POST['name']) ? htmlspecialchars($_POST['name']) : '';
$email = isset($_POST['email']) ? htmlspecialchars($_POST['email']) : '';
$pass = isset($_POST['password']) ? $_POST['password'] : '';

if (empty($name) || empty($email) || empty($pass)) {
    echo json_encode(["success" => false, "message" => "Data tidak lengkap"]);
    exit;
}

// Check if email already exists
$check_sql = "SELECT id FROM users WHERE email = ?";
$check_stmt = $conn->prepare($check_sql);
$check_stmt->bind_param("s", $email);
$check_stmt->execute();
$check_result = $check_stmt->get_result();

if ($check_result->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Email sudah terdaftar"]);
    $check_stmt->close();
    $conn->close();
    exit;
}

// Hash password for security
$hashed_password = password_hash($pass, PASSWORD_DEFAULT);

// Insert new user
$insert_sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
$insert_stmt = $conn->prepare($insert_sql);
$insert_stmt->bind_param("sss", $name, $email, $hashed_password);

if ($insert_stmt->execute()) {
    echo json_encode([
        "success" => true,
        "message" => "Registrasi berhasil",
        "id" => $conn->insert_id,
        "name" => $name,
        "email" => $email
    ]);
} else {
    echo json_encode([
        "success" => false,
        "message" => "Error: " . $insert_stmt->error
    ]);
}

$check_stmt->close();
$insert_stmt->close();
$conn->close();
?>

