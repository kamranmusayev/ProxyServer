import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;



public class ServerProxy implements Runnable{
	InputStream proxyToClientInput_Stream;
	OutputStream proxyToServerOutput_Stream;
	
	public ServerProxy(InputStream proxyToClient, OutputStream proxyToServer) {
		
		this.proxyToClientInput_Stream = proxyToClient;
		this.proxyToServerOutput_Stream = proxyToServer;
		new Thread(this).start();
	}
	
	
	
	
	@Override
	public void run() {
		try {
			// Read byte by byte from client and send directly to server
			byte[] buffer = new byte[4096];
			int read = 0;
			while(read >= 0){
				while(!Proxy.running) {System.out.print("");}
				read = proxyToClientInput_Stream.read(buffer);
				if (read > 0) {
					proxyToServerOutput_Stream.write(buffer, 0, read);
					if (proxyToClientInput_Stream.available() < 1) {
						proxyToServerOutput_Stream.flush();
					}
				}
			} 
		}
		catch (SocketTimeoutException ste) {
			// TODO: handle exception
			ste.printStackTrace();
		}
		catch (IOException e) {
			System.out.println("Proxy to client HTTPS read timed out");
			//e.printStackTrace();
		}
		
	}
	
}

