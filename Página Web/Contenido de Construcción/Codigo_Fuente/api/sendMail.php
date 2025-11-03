<?php
header('Content-Type: application/json; charset=utf-8');

$body = json_decode(file_get_contents('php://input'), true);

if (!$body) {
    http_response_code(400);
    echo json_encode(['ok'=>false,'msg'=>'Solicitud inválida']);
    exit;
}

$email = trim($body['email'] ?? '');
$name = trim($body['name'] ?? '');
$lastname = trim($body['last'] ?? '');
$total = $body['total'] ?? 0;
$items = $body['items'] ?? [];

if (!$email || !$name || !$lastname || !$items) {
    http_response_code(400);
    echo json_encode(['ok'=>false,'msg'=>'Faltan datos']);
    exit;
}

// Tu API Key de Brevo
$apiKey = 'API-KEY(REEMPLAZAR)';

// Generar lista de items en HTML
$itemsHtml = '';
foreach ($items as $it) {
    $itemsHtml .= "<li>{$it['title']} — {$it['qty']} × \$" . number_format($it['price'],2) . "</li>";
}

// Datos del correo
$data = [
    "sender" => ["name" => "VinylShop", "email" => "contacto.vinylshop@gmail.com"],
    "to" => [["email" => $email, "name" => $name]],
    "subject" => "Confirmación de compra - VinylShop",
    "htmlContent" => "
        <h2>Gracias por tu compra, $name $lastname 🎶</h2>
        <p>Tu pago se ha recibido correctamente.</p>
        <p><strong>Items:</strong></p><ul>$itemsHtml</ul>
        <p><strong>Total:</strong> \$" . number_format($total,2) . "</p>
        <p>Pronto recibirás tu pedido. ¡Gracias por confiar en nosotros!</p>
    "
];

// Enviar usando cURL
$ch = curl_init('https://api.brevo.com/v3/smtp/email');
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    'accept: application/json',
    'api-key: ' . $apiKey,
    'content-type: application/json'
]);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($data));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

$response = curl_exec($ch);
$code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
curl_close($ch);

if ($code === 201) {
    echo json_encode(['ok'=>true,'msg'=>'Correo enviado correctamente']);
} else {
    echo json_encode(['ok'=>false,'msg'=>'Error al enviar correo','response'=>$response]);
}