<?php
$url = "http://localhost:8000/test";
//$obj = json_decode($json);
//echo $obj->speed;
$result = file_get_contents($url, false, stream_context_create(array(
    'http' => array(
        'method'  => 'POST',
        'header'  => 'Content-type: application/x-www-form-urlencoded',
        'content' => http_build_query($_POST)
    )
)));
$obj = json_decode($result);
echo $obj->speed;
?>
