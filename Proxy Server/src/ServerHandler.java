import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;




public class ServerHandler implements Runnable{
	
	private DataInputStream inFromClient;
	private DataOutputStream outToClient;
	private String host,path;
	private Thread thread;
	private Printer printer;
	private int port_number = 80;
	public ServerHandler(Socket s) 
	{
		try {
			if(Proxy.running) {
				printer = new Printer();
	
				printer.add("A connection from a client is initiated...");
				
				inFromClient = new DataInputStream(s.getInputStream());
				outToClient = new DataOutputStream(s.getOutputStream());
	
				thread = new Thread(this);
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String get_Header(DataInputStream in ) throws NumberFormatException, IOException 
	{
		byte[] header = new byte[1024];

		int data;
		int h = 0;
		
		int content_length =-1;
		
		while ((data = in.read()) != -1) {
			header[h++] = (byte) data;
			
			
			if(content_length == 0 ) { break;}
			content_length = (content_length > 0)? content_length - 1 : content_length;
			
			if ( header[h - 1] == '\n' && header[h - 2] == '\r' && header[h - 3] == '\n' && header[h - 4] == '\r') {
				if(header[0] == 'P') {
					
					String s = new String(header, 0, h);
					
					try {
					int content_length_index = s.indexOf("Content-Length:");
					content_length = Integer.parseInt(s.substring(content_length_index+16,s.indexOf("\r\n",content_length_index)))-1;
					//System.out.println("Content _Length is: "+content_length);
					}
					catch(StringIndexOutOfBoundsException e) {
						System.out.println("No Content Length found");
						System.out.println(s);
					}
					catch(NumberFormatException nfe) 
					{
						int content_length_index = s.indexOf("Content-Length:");
						System.out.println(s.substring(content_length_index+16,s.indexOf("\r\n",content_length_index)-2));
					}
					
					
					continue;
					}
				
				break;
			}
		}
		
		return new String(header, 0, h);
	}
	
	
	public MimeHeader makeMimeHeader(String type, int length) 
	{
		MimeHeader mh = new MimeHeader();
		Date d = new Date();
		TimeZone gmt = TimeZone.getTimeZone("GMT");
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss zzz");
		sdf.setTimeZone(gmt);
		String sdf_date = sdf.format(d);

		mh.put("Date",sdf_date);
		mh.put("Server","CSE-471" );
		mh.put("Content-type", type);
		
		if (length >= 0)
			mh.put("Content-Length", String.valueOf(length));
		return mh;
	}
	
	public String createErrorPage(int code, String msg, String address) 
	{
		String html_page = "";
		html_page += "<html>\r\n" + "<body>\r\n" + "<h1>"+code+" "+ msg + "</h1>\r\n" + "</body>\r\n" + "</html>\r\n";
		MimeHeader mh = makeMimeHeader("text/html", html_page.length());
		Response hr = new Response(code,msg,mh); 
		
		return hr + html_page;
	}
	
	
	
	private void handleProxy( MimeHeader reqMH,String request) throws IOException //Handle Proxy
	{
		//printer.add("\r\nInitiating the server connection");
		reqMH.put("User-Agent", reqMH.get("User-Agent") + " via CSE471 Proxy");
		
		Socket sSocket = new Socket(host, port_number);
		DataInputStream inFromServer = new DataInputStream(sSocket.getInputStream());
		DataOutputStream outToServer = new DataOutputStream(sSocket.getOutputStream());
		ByteArrayOutputStream bAOS = new ByteArrayOutputStream(10000);
		int temp;
		byte[] buffer = new byte[1024];
		byte[] response;
		String raw_Response,responseHeader;
		switch(request.toLowerCase()) {
		case "connect":
			printer.add("\r\nSending to server...\r\n" + "CONNECT " + path + " HTTP/1.1\r\n" + reqMH + "\r\n");
			new ServerProxy(inFromClient , outToServer);
			
			String s = "HTTP/1.0 200 Connection established\r\n" + "Proxy-Agent: CSE-471 Proxy \r\n" + "\r\n";
			
			outToClient.write(s.getBytes());
			outToClient.flush();
			
			do {
				while(!Proxy.running) {System.out.print("");}
				temp = inFromServer.read(buffer);
				if (temp >0) {
					outToClient.write(buffer, 0, temp);
					if (inFromServer.available() < 1) {
						outToClient.flush();
					}
					
				}
			} while (temp >= 0);
			
			if(sSocket != null) sSocket.close();
			printer.add("Served http://" + host + path + "\r\nExiting ServerHelper thread...\r\n" + "\r\n----------------------------------------------------" + "\r\n");
			
			break;		
			
		case "get":
			
			if( Cache.check_cache(host+path)) {//Cache hit
				try {
					outToServer.writeBytes("HEAD " + path + " HTTP/1.1\r\n" + reqMH+"If-Modified-Since: " + 
							Cache.getCached_Objects(host+path).getLast_modified()+"\r\n"+"\r\n");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				byte[] cache_buffer = new byte[2048];
				
				int a = 0;
				ByteArrayOutputStream cache_bAOS = new ByteArrayOutputStream(10000);
				while ((a = inFromServer.read(cache_buffer)) != -1) {
					cache_bAOS.write(cache_buffer, 0, a);
				}
				byte[] last_modified_response = cache_bAOS.toByteArray();


				if(new String(last_modified_response).contains("304")) {//Not modified
					
					byte[] cached_data = null;
					try {
						cached_data = Cache.getCached_Objects(host+path).getCached_resource();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				 
					outToClient.write(cached_data);
					System.out.println("Ben var kullanmak cache"+ "\r\n"); //Degistir!!!!
					
					outToClient.close();
					sSocket.close();
					return;
				}
				Cache.Delete_object(host+path);
				
			}
			printer.add("\r\nSending to server...\r\n" + "GET " + path + " HTTP/1.1\r\n" + reqMH + "\r\n");
			outToServer.writeBytes("GET " + path + " HTTP/1.1\r\n" + reqMH + "\r\n");
			printer.add("HTTP request sent to: " + host);
			
			while ((temp = inFromServer.read(buffer)) != -1) bAOS.write(buffer, 0, temp);
			response = bAOS.toByteArray();
			raw_Response = new String(response);
			responseHeader = raw_Response.substring(0, raw_Response.indexOf("\r\n\r\n"));
			
			printer.add("\r\nResponse Header\r\n" + responseHeader);
			printer.add("\r\n\r\nGot " + response.length + " bytes of response data...\r\n" + "Sending it back to the client...\r\n");
			
			if(responseHeader.contains("Last-Modified:") && !Cache.check_cache(host+path)) {
				
				int last_modified_index = responseHeader.indexOf("Last-Modified:");
				
				Cache.AddtoCache(host+path, responseHeader.substring(last_modified_index+15,responseHeader.indexOf("\n",last_modified_index) )  , 
						response);
				
			}
			
			
			outToClient.write(response);
			outToClient.close();
			sSocket.close();
			
			break;
		case "head":
			printer.add("\r\nSending to server...\r\n" + "HEAD " + path + " HTTP/1.1\r\n" + reqMH + "\r\n");
			outToServer.writeBytes("HEAD "+ path +" HTTP/1.1\r\n" + reqMH + "\r\n");
			printer.add("HTTP request sent to: " + host);
			
			while ((temp = inFromServer.read(buffer)) != -1) bAOS.write(buffer, 0, temp);
			response = bAOS.toByteArray();
			raw_Response = new String(response);
			System.out.println("----------------------");
			System.out.println(raw_Response);
			System.out.println("----------------------");
			responseHeader = raw_Response.substring(0, raw_Response.indexOf("\r\n\r\n"));
			
			printer.add("\r\nResponse Header\r\n" + responseHeader);
			printer.add("\r\n\r\nGot " + response.length + " bytes of response data...\r\n" + "Sending it back to the client...\r\n");
			outToClient.write(response);
			outToClient.close();
			sSocket.close();
			
			break;
		case "post":
			
			//printer.add("\r\nSending to server...\r\n" + "POST " + path + " HTTP/1.1\r\n" + reqMH + "\r\n"+ reqMH.get("data").getBytes());
			
			outToServer.writeBytes("POST "+ path +" HTTP/1.1\r\n" + reqMH + "\r\n" + reqMH.get("data"));
			//printer.add("HTTP request sent to: " + host);
			while ((temp = inFromServer.read(buffer)) != -1) bAOS.write(buffer, 0, temp);
			
			response = bAOS.toByteArray();
			raw_Response = new String(response);
			
			responseHeader = raw_Response.substring(0, raw_Response.indexOf("\r\n\r\n"));
			
			//printer.add("\r\nResponse Header\r\n" + responseHeader);
			printer.add("\r\n\r\nGot " + response.length + " bytes of response data...\r\n" + "Sending it back to the client...\r\n");
			outToClient.write(response);
			outToClient.close();
			sSocket.close();
			
			break;
		}
	}
	
	@Override
	public void run() {
		try {
			String header = get_Header(inFromClient);
			
			int initial = header.indexOf(" ");
			int next_space = header.indexOf(" ",initial+1);
			int endl = header.indexOf('\r');
			
			String reqHeaderRemainingLines = header.substring(endl + 2);
			MimeHeader reqMH = new MimeHeader(reqHeaderRemainingLines);
			String url = header.substring(initial + 1, next_space);
			
			String method = header.substring(0, initial);
			host = reqMH.get("Host");
			reqMH.put("Connection", "close");
			
			if(!method.equalsIgnoreCase("connect")) { path = url.substring(7); path = "/" + path.substring(path.indexOf("/")+1); }
			
			if (Proxy.forbiddenAddresses.contains(host)) {
				printer.add("Connection blocked to the host due to the proxy policy");
				outToClient.writeBytes(createErrorPage(401, "Not Authorized", method));	
				return;
			} 

			switch(method.toLowerCase()) {
				case "get":
					printer.add("Client requests...\r\nHost: " + host + "\r\nPath: " + path);
					handleProxy(reqMH,"get");
					break;
				case "post":
					printer.add("Client requests...\r\nHost: " + host + "\r\nPath: " + path);
					handleProxy(reqMH,"post");
					break;
				case "head":
					printer.add("Client requests...\r\nHost: " + host + "\r\nPath: " + path);
					handleProxy(reqMH,"head");
					break;
				case "connect":
					//System.out.println(url);
					
					url = url.substring(0,url.indexOf(":"));
					
					path = url.substring(7);
					path = "/" + path.substring(path.indexOf("/")+1);
					
					host = url;
				
					printer.add("Client requests...\r\nHost: " + host + "\r\nPath: " + path);
					port_number = 443;
					handleProxy(reqMH, "connect");
					break;
				default:
					printer.add("Requested method " + method + " is not allowed on proxy server");
					outToClient.writeBytes(createErrorPage(405, "Method Not Allowed", method));	
			}
			
			printer.removeThread();
			
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}

}
