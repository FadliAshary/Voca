<?php
// install_db.php
// Run this once to create the database and table. Place in your XAMPP htdocs/voca_db/ and open in browser.

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');

$config = require __DIR__ . '/config.php';

$mysqli = new mysqli($config['host'], $config['username'], $config['password']);
if ($mysqli->connect_error) {
    echo json_encode(['success' => false, 'message' => 'Connection failed: ' . $mysqli->connect_error]);
    exit;
}

$dbname = $mysqli->real_escape_string($config['dbname']);
$sql = "CREATE DATABASE IF NOT EXISTS `{$dbname}` DEFAULT CHARACTER SET {$config['charset']} COLLATE {$config['charset']}_unicode_ci";
if (!$mysqli->query($sql)) {
    echo json_encode(['success' => false, 'message' => 'Could not create database: ' . $mysqli->error]);
    $mysqli->close();
    exit;
}

// Select the database
$mysqli->select_db($dbname);

$createTable = "CREATE TABLE IF NOT EXISTS `finance` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `amount` DECIMAL(15,2) NOT NULL,
  `type` ENUM('income','expense') NOT NULL,
  `category` VARCHAR(100) DEFAULT NULL,
  `date` DATE NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET={$config['charset']} COLLATE {$config['charset']}_unicode_ci";

if (!$mysqli->query($createTable)) {
    echo json_encode(['success' => false, 'message' => 'Could not create table: ' . $mysqli->error]);
    $mysqli->close();
    exit;
}

$mysqli->close();

echo json_encode(['success' => true, 'message' => 'Database and table ready', 'database' => $config['dbname']]);

