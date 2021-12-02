<?php
for ($i=0; $i<=1000; $i++) {
    $t1 = microtime(true);
    $url = "http://localhost:8000/test";
//$obj = json_decode($json);
//echo $obj->speed;
    $result = file_get_contents($url, false, stream_context_create(array(
        'http' => array(
            'method' => 'POST',
            'header' => 'Content-type: application/x-www-form-urlencoded',
            'content' => http_build_query(["pressed" => "true"])
        )
    )));
    $obj = json_decode($result);
    echo $obj->speed;
    $t2 = microtime(true);
    $difft = $t2-$t1;
    echo " Время: ".$difft;
    echo "<br>";
}
?>
