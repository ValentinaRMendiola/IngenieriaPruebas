<?php
header('Content-Type: application/json; charset=utf-8');
session_start();

$USERS_FILE = __DIR__ . '/users.json';

// read input
$body = json_decode(file_get_contents('php://input'), true);
if (!$body) {
  http_response_code(400);
  echo json_encode(['ok' => false, 'msg' => 'Solicitud inválida']);
  exit;
}

$name = trim($body['name'] ?? '');
$lastname = trim($body['last'] ?? '');
$email = strtolower(trim($body['email'] ?? ''));
$password = $body['password'] ?? '';

if (!$name || !$lastname || !$email || !$password) {
  http_response_code(400);
  echo json_encode(['ok' => false, 'msg' => 'Completa todos los campos']);
  exit;
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
  http_response_code(400);
  echo json_encode(['ok'=>false, 'msg'=>'Email inválido']);
  exit;
}

// load users
$users = [];
if (file_exists($USERS_FILE)) {
    $raw = file_get_contents($USERS_FILE);
    $users = $raw ? json_decode($raw, true) : [];
    if (!is_array($users)) $users = [];
}

// check exists
foreach ($users as $u) {
    if (isset($u['email']) && strtolower($u['email']) === $email) {
        http_response_code(409);
        echo json_encode(['ok'=>false, 'msg'=>'Ya existe una cuenta con ese correo']);
        exit;
    }
}

// hash password
$hash = password_hash($password, PASSWORD_DEFAULT);

// add user
$new = [
    'id' => uniqid('u_', true),
    'name' => $name,
    'lastname' => $lastname,
    'email' => $email,
    'password_hash' => $hash,
    'created_at' => date('c')
];
$users[] = $new;

// save (atomic-ish with lock)
$fp = fopen($USERS_FILE, 'c+');
if (!$fp) {
    http_response_code(500);
    echo json_encode(['ok'=>false, 'msg'=>'No se puede abrir archivo de usuarios']);
    exit;
}
if (flock($fp, LOCK_EX)) {
    ftruncate($fp, 0);
    rewind($fp);
    fwrite($fp, json_encode($users, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE));
    fflush($fp);
    flock($fp, LOCK_UN);
}
fclose($fp);

// set session (logged in)
unset($new['password_hash']);
$_SESSION['user'] = $new;

echo json_encode(['ok'=>true, 'user'=>$new]);