//+-----------------------------------------------------------------------------------------------+
//|                                                                            DukascopyMT4-5.mq4 |
//+-----------------------------------------------------------------------------------------------+
   
#property copyright   "breakermind.com All rights reserved."
#property link        "https://breakermind.com"

// login to page
input string Username    = "user";
input string Password    = "pass";

// copy user
input string CopyUser    = "woow";
// Balance for user in -> 1 = 100$
input string Capital = "1 = 100$, 2 = 200$ ... 10 = 1000$ ...";
input int MaxCapitalUSD = 1;
// page path
input bool   ssl = false;
input string url="localhost";
input int    RefreshMilliseconds = 500;
string trade_mode;
string currency;
string company;
string apiurl;
string barstart;
string newbar;
string balance,equity;

int OnInit()
{          
   EventSetMillisecondTimer(RefreshMilliseconds);    
   
   // api url http or https(ssl)
   if(ssl){
   apiurl = "https://" + url + "/api.php"; 
   }
   if(!ssl){
   apiurl = "http://" + url + "/api.php"; 
   }  
   // Name of the company
   company=AccountInfoString(ACCOUNT_COMPANY);
   // Account currency
   currency=AccountInfoString(ACCOUNT_CURRENCY);
   // Demo, contest or real account
   ENUM_ACCOUNT_TRADE_MODE account_type=(ENUM_ACCOUNT_TRADE_MODE)AccountInfoInteger(ACCOUNT_TRADE_MODE);
   // Now transform the value
   switch(account_type)
     {
      case  ACCOUNT_TRADE_MODE_DEMO:
         trade_mode="demo";
         break;
      case  ACCOUNT_TRADE_MODE_CONTEST:
         trade_mode="contest";
         break;
      default:
         trade_mode="real";
         break;
     } 
   return(0);      
}
  
void OnDeinit(const int reason)
{  
   Print("Strategy stoped.");
}
  
void OnTimer()
{
// load positions from server
loadPositions();          
}

void loadPositions(){
      // declaration - variables
      char post[];
      char result[];
      string headers;
      int res;   
      string send = "";
      
      // send username password and user to copy from server
      // post data to send to server
      send = 
      "&user=" + Username + 
      "&pass=" + Password + 
      "&copyuser=" + CopyUser +       
      "&broker=" +company+
      "&mode="+ trade_mode + 
      "&accountcurrency=" + currency + 
      "&accountid=" +AccountNumber() + 
      "&time=" + TimeCurrent();   
      
      
      Print("Client's send: ",send);
      StringToCharArray(send,post);
      // reset last error
      ResetLastError();
      // post data to HTTP server API
      res=WebRequest("POST",apiurl,NULL,NULL,50,post,ArraySize(post),result,headers);
      
      // check errors
      if(res==-1)
        {
         Print("Error code =",GetLastError());
         // maybe the URL is not added, show message to add it
         Print("Add address '"+apiurl+"' in Expert Advisors tab of the Options window","Error",MB_ICONINFORMATION);
        }
      else
        {
         // successful
         Print("Server response:",CharArrayToString(result,0));
        }   

}
