<?php
header('Content-Type: application/json; charset=utf-8');
session_start();

$USERS_FILE = __DIR__ . '/users.json';

$body = json_decode(file_get_contents('php://input'), true);
if (!$body) {
  http_response_code(400);
  echo json_encode(['ok' => false, 'msg' => 'Solicitud inválida']);
  exit;
}

$email = strtolower(trim($body['email'] ?? ''));
$password = $body['password'] ?? '';

if (!$email || !$password) {
  http_response_code(400);
  echo json_encode(['ok' => false, 'msg' => 'Completa correo y contraseña']);
  exit;
}

if (!file_exists($USERS_FILE)) {
  http_response_code(401);
  echo json_encode(['ok'=>false, 'msg'=>'Credenciales incorrectas']);
  exit;
}

$raw = file_get_contents($USERS_FILE);
$users = $raw ? json_decode($raw, true) : [];
$found = null;
foreach ($users as $u) {
  if (isset($u['email']) && strtolower($u['email']) === $email) {
    $found = $u;
    break;
  }
}

if (!$found || !isset($found['password_hash']) || !password_verify($password, $found['password_hash'])) {
  http_response_code(401);
  echo json_encode(['ok'=>false, 'msg'=>'Credenciales incorrectas']);
  exit;
}

// login ok: remove hash for response and store in session
$user = $found;
unset($user['password_hash']);
$_SESSION['user'] = $user;

echo json_encode(['ok'=>true, 'user'=>$user]);