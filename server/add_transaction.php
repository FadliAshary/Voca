<?php
// add_transaction.php
// Accepts POST parameters and inserts a finance record into the `finance` table.

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

$config = require __DIR__ . '/config.php';

$mysqli = new mysqli($config['host'], $config['username'], $config['password'], $config['dbname']);
if ($mysqli->connect_error) {
    echo json_encode(['success' => false, 'message' => 'Connection failed: ' . $mysqli->connect_error]);
    exit;
}

$mysqli->set_charset($config['charset']);

// Read POST data (supports application/x-www-form-urlencoded or multipart/form-data)
$title = isset($_POST['title']) ? trim($_POST['title']) : '';
$amount = isset($_POST['amount']) ? $_POST['amount'] : null;
$type = isset($_POST['type']) ? trim($_POST['type']) : '';
$category = isset($_POST['category']) ? trim($_POST['category']) : null;
$date = isset($_POST['date']) ? trim($_POST['date']) : null;

// Basic validation
$errors = [];
if ($title === '') $errors[] = 'title is required';
if ($amount === null || !is_numeric($amount)) $errors[] = 'amount is required and must be a number';
if (!in_array($type, ['income', 'expense'], true)) $errors[] = 'type must be "income" or "expense"';
if ($date === null) $errors[] = 'date is required (format YYYY-MM-DD)';

if (!empty($errors)) {
    echo json_encode(['success' => false, 'message' => 'Validation failed', 'errors' => $errors]);
    $mysqli->close();
    exit;
}

// Normalize values
$amount = (float) $amount;
$category = $category === '' ? null : $category;

// Prepared statement to insert
$stmt = $mysqli->prepare("INSERT INTO finance (title, amount, type, category, date) VALUES (?, ?, ?, ?, ?)");
if (!$stmt) {
    echo json_encode(['success' => false, 'message' => 'Prepare failed: ' . $mysqli->error]);
    $mysqli->close();
    exit;
}

$stmt->bind_param('sdsss', $title, $amount, $type, $category, $date);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'Transaksi berhasil disimpan', 'id' => $stmt->insert_id]);
} else {
    echo json_encode(['success' => false, 'message' => 'Execute failed: ' . $stmt->error]);
}

$stmt->close();
$mysqli->close();

