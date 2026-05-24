<?php
$host = "localhost";
$user = "root";
$pass = "";
$db   = "voca_db";

$conn = new mysqli($host, $user, $pass, $db);

if ($conn->connect_error) {
    die(json_encode(["success" => false, "message" => "Koneksi database gagal: " . $conn->connect_error]));
}
?>