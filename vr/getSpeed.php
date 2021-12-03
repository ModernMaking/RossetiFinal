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
foreach ($obj as $key=>$value)
{
    $value = preg_replace_callback('/\\\\u([0-9a-fA-F]{4})/', function ($match) {
        return mb_convert_encoding(pack('H*', $match[1]), 'UTF-8', 'UCS-2BE');
    }, $value);
    $obj->$key = urldecode($value);
}
$json = json_encode($obj);

echo $json;
?>
