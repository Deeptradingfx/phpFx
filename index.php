<?php
$pos = $_POST["line"];
$balance = $_POST["balance"];
$equity = $_POST["equity"];
file_put_contents('positions.txt', $pos);
echo $pos;
?>
