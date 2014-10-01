<?php
// https redirect
function redirectSSL()
{
    if($_SERVER["HTTPS"] != "on") {
       header("HTTP/1.1 301 Moved Permanently");
       header("Location: https://" . $_SERVER["SERVER_NAME"] . $_SERVER["REQUEST_URI"]);
       exit();
    }    
}

function setSessionUser($user = "")
{
  $_SESSION['active'] = 1;
  $_SESSION['ip'] = $_SERVER['REMOTE_ADDR'];
  $_SESSION['user'] = $user;
}

function isSessionSet()
{
	if(!isset($_SESSION['active']) && $_SESSION['active'] != 1 && $_SESSION['ip'] != $_SERVER['REMOTE_ADDR']){
	  session_destroy();
	  session_unset();
	  header('Location: index.php');
	  die("Reset session incorrect data!");
	}
}

function secure_session_start()
{
    $session_name = 'jhsdajdasewewy6w3u4f4r';   // Set a custom session name
    $secure = "SECURE";
    // This stops JavaScript being able to access the session id.
    $httponly = true;
    // Forces sessions to only use cookies.
    if (ini_set('session.use_only_cookies', 1) === FALSE) {
        header("Location: error.php?err=Could not initiate a safe session (ini_set)");
        exit();
    }
    ini_set('session.cookie_lifetime',3600);
	ini_set('session.gc_maxlifetime',3600);
    // Gets current cookies params.
    $cookieParams = session_get_cookie_params();
    session_set_cookie_params($cookieParams["lifetime"],
        $cookieParams["path"], 
        $cookieParams["domain"], 
        $secure,
        $httponly);
    // Sets the session name to the one set above.
    session_name($session_name);
    session_start();            // Start the PHP session 
    session_regenerate_id();    // regenerated the session, delete the old one. 
}

function isUserExist($user = "", $pass = "")
{
	if($user != "" && $pass != ""){
		$h = 'localhost';
		$u = 'root';
		$p = 'toor';
		$db = 'db';
		$link = mysql_connect($h,$u,$p) or die('DB_ERROR');
		mysql_select_db($db, $link) or die('DB_ERROR');
		$user = mysql_real_escape_string($user);
		$pass = mysql_real_escape_string($pass);
		mysql_query("SET character_set_results = 'utf8', character_set_client = 'utf8', character_set_connection = 'utf8', character_set_database = 'utf8', character_set_server = 'utf8'", $link);
		$result = mysql_query("SELECT * FROM users where alias = '$user' AND pass = '$pass'", $link);
	    $num_rows = mysql_num_rows($result);    
	    mysql_close($link);
	    return $num_rows;
	}
}

function logTofile()
{
    $time = date("Y-m-d H:i:s");
    $day = date("Y-m-d");
    $log = "log/".$day."-log.db";
    $url = $time."|".$_SERVER['REMOTE_ADDR']."|".$_SERVER['HTTP_HOST']."|".$_SERVER['REQUEST_URI']."\r\n";
    file_put_contents($log, $url, FILE_APPEND | LOCK_EX);
}

 function valid_email($email)
 {
    if(filter_var($email, FILTER_VALIDATE_EMAIL)) {
        return true;
    } else {
        return false;
    }
}

function randomString()
{
	return substr( "0123456789abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ" ,mt_rand( 5 ,60 ) ,1 ) .substr( md5( time() ), 1);
}


/*
  if (!empty($_SERVER['HTTPS']) && ('on' != $_SERVER['HTTPS'])) {
    $uri = 'https://';
  } else {
    $uri = 'https://';
  }
  $uri .= $_SERVER['HTTP_HOST'];
  //turn on ssl redirect
  //header('Location: '.$uri.'');
*/
  

?>
