package singlejartest;

import com.dukascopy.api.system.ISystemListener;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ClientFactory;
import com.dukascopy.api.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Main {
	// login credentials contest demo account
    private static String userName = "DEMO";
    private static String password = "PASS";
	private static int forceSsl = 1; 
	// server file path
	private static String postUrl = "https://localhost/index.php";
	// user browser
	private static String USER_AGENT = "Mozilla/5.0";
	
	// dukascopy demo account login    
    private static String jnlpUrl = "https://www.dukascopy.com/client/demo/jclient/jforex.jnlp";
    private static String accountId = "0";
    private static double accountBalance = 0;
    private static double accountEquity = 0;
    private static String serverResult = "";
    // log
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    
    // open positions string
    private static String pos ="";
    
    //static String[] columns = {"AccountId","OpenTime", "Id", "Instrument", "Side", "Amount", "Open Price", "Stop Loss", "Take Profit","Label","Comment"};
    //static String[][] data = new String[50][11];

    public static void main(String[] args) throws Exception {       


        final IClient client = ClientFactory.getDefaultInstance();
        client.setSystemListener(new ISystemListener() {
            private int lightReconnects = 3;

            public void onStart(long procid) {
                //IConsole console = context.getConsole();
                LOGGER.info("Welcome");  
            }

            public void onStop(long processId) {
                LOGGER.info("Strategy stopped: " + processId);  
                if (client.getStartedStrategies().size() == 0) {
                    System.exit(0);
                }
            }

            @Override
            public void onConnect() {
                LOGGER.info("Connected");
                lightReconnects = 10000000;
            }

            @Override
            public void onDisconnect() {
                LOGGER.warn("Disconnected");
                if (lightReconnects > 0) {
                    LOGGER.error("TRY TO RECONNECT, reconnects left: " + lightReconnects);
                    client.reconnect();
                    --lightReconnects;
                } else {
                    try {
                        //sleep for 10 seconds before attempting to reconnect
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                    try {
                        client.connect(jnlpUrl, userName, password);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        });

        LOGGER.info("Connecting...");
        //connect to the server using jnlp, user name and password
        client.connect(jnlpUrl, userName, password);

        //wait for it to connect
        int i = 10; //wait max ten seconds
        while (i > 0 && !client.isConnected()) {
            Thread.sleep(1000);
            i--;
        }
        if (!client.isConnected()) {
            LOGGER.error("Failed to connect Dukascopy servers");
            System.exit(1);
        }

        //subscribe to the instruments
        Set<Instrument> instruments = new HashSet<Instrument>();
        instruments.add(Instrument.EURUSD);
        //LOGGER.info("Subscribing instruments...");
        client.setSubscribedInstruments(instruments);

        //start the strategy
        LOGGER.info("Starting ...");
        final long strategyId = client.startStrategy(new IStrategy(){
            public Instrument instrument = Instrument.EURUSD;
            private IConsole console;
            private IEngine engine;


            public void onStart(IContext context) throws JFException {        
                console = context.getConsole();    
                engine = context.getEngine();
             
           // force ssl     
             if(forceSsl == 1){   
             // Create a trust manager that does not validate certificate chains
                TrustManager[] trustAllCerts = new TrustManager[] { 
                    new X509TrustManager() {     
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
                            return new java.security.cert.X509Certificate[0];
                        } 
                        public void checkClientTrusted( 
                            java.security.cert.X509Certificate[] certs, String authType) {
                            } 
                        public void checkServerTrusted( 
                            java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    } 
                }; 

                // Install the all-trusting trust manager
                try {
                    SSLContext sc = SSLContext.getInstance("SSL"); 
                    sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                } catch (GeneralSecurityException e) {
                } 
             }// end force ssl   

             
            }
            public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
                if ( instrument == this.instrument){
                    //console.getOut().println(" bar: " + period  + " " + askBar);
                }
            }

            public void onAccount(IAccount account) throws JFException {   
            	accountId = account.getAccountId();
            	accountBalance = account.getBalance();
            	accountEquity = account.getEquity();
            }
            
			public void onTick(Instrument instrument, ITick tick) throws JFException { 

                try {                   
                pos = "";	
                for(IOrder o : engine.getOrders()){
                    if(o.getProfitLossInUSD() != 987654231){
                        console.getOut().println("Order: " + o.getInstrument() + " " + o.getProfitLossInPips() + " " + o.getOrderCommand());

                        // Get order           
                       
					    String Account = "" + accountId;
                        String OpenTime = "" + o.getFillTime();
                        String Id = "" + o.getId();
                        String Instrument = "" + o.getInstrument();
                        String Isbuy = "" + o.getOrderCommand().isLong();
                        String Volume = "" + o.getAmount();
                        String Open = "" + o.getOpenPrice();
                        String Sl = "" + o.getStopLossPrice(); 
                        String Tp = "" + o.getTakeProfitPrice();
                        String Comment = "" + o.getComment();
                        String Label = "" + o.getLabel();
                        
                        pos = pos +
                        	  Account + ";" +	
                        	  OpenTime + ";" + 
                        	  Id + ";" + 
                        	  Instrument + ";" + 
                        	  Isbuy + ";" + 
                        	  Volume + ";" + 
                        	  Open + ";" + 
                        	  Sl + ";" + 
                        	  Tp + ";" + 
                        	  Label + ";" + 
                        	  Comment + "[space]"; 
                        
                    }
                }

                //=================================================================== send get == need commerciall ssl like startssl.com and serveralias and servername in virtualhost
                //URL hp = new URL("http://localhost/index.php?line="+pos);
                //HttpURLConnection hpCon = (HttpURLConnection) hp.openConnection();
                //boolean isProxy = hpCon.usingProxy();
                //System.out.println("is using proxy " + isProxy);
                //InputStream obj = hpCon.getInputStream();
                //BufferedReader br = new BufferedReader(new InputStreamReader(obj));
                //String s;
                //while ((s = br.readLine()) != null) {
                //System.out.println("From Server : " + s);
                //}
                //===================================================================== end         

                //===================================================================== send post request to server        		
        		
               	
                    console.getOut().println("=====================================================================================" );
                    System.out.println("Equity: " + accountEquity);
                    System.out.println("Balance: " + accountBalance);
                    console.getOut().println("=====================================================================================" );
                    
                    console.getOut().println("=====================================================================================" );
                    System.out.println("From Client : " + pos);
                    console.getOut().println("=====================================================================================" );
                	
                	URL obj = new URL(postUrl);
	        		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
	         
	        		//add reuqest header
	        		con.setRequestMethod("POST");
	        		con.setRequestProperty("User-Agent", USER_AGENT);
	        		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	         
	        		String urlParameters = "line=" + pos + "&balance=" + accountBalance + "&equity=" + accountEquity;
	         
	        		// Send post request
	        		con.setDoOutput(true);
	        		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	        		wr.writeBytes(urlParameters);
	        		wr.flush();
	        		wr.close();
	         
	        		//int responseCode = con.getResponseCode();
	        		//System.out.println("\nSending 'POST' request to URL : " + url);
	        		//System.out.println("Post parameters : " + urlParameters);
	        		//System.out.println("Response Code : " + responseCode);
	         
	        		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	        		String inputLine;
	        		StringBuffer response = new StringBuffer();
	         
	        		while ((inputLine = in.readLine()) != null) {
	        			response.append(inputLine);
	        		}
	        		in.close();
	         
	        		// save result
	        		serverResult = response.toString();
	        		//print result        		
	        		System.out.println("From Server : " + serverResult );

                
              } catch (Exception e) {
                  console.getErr().println(e.getMessage());
                  e.printStackTrace(console.getErr());
                 // context.stop();
              }

            console.getOut().println("=====================================================================================" );

            }
            public void onMessage(IMessage message) throws JFException {    }
            
            public void onStop() throws JFException {    }
        });
        //now it's running

        //every second check if "stop" had been typed in the console - if so - then stop program
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {    

                Scanner s = new Scanner(System.in);             
                while(true){
                    while(s.hasNext()){
                        String str = s.next();
                        if(str.equalsIgnoreCase("stop")){
                            System.out.println("Strategy stop by console command.");
                            client.stopStrategy(strategyId);
                            s.close();
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            });
        thread.start();

    }
}   
   


