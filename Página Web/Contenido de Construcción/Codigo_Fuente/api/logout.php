<?php
header('Content-Type: application/json; charset=utf-8');
session_start();

// destroy session user only (leave session if other data)
if (isset($_SESSION['user'])) unset($_SESSION['user']);

// optionally destroy session completely
// session_unset();
// session_destroy();

echo json_encode(['ok'=>true]);