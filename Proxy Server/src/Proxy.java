import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import javax.swing.JLabel;



@SuppressWarnings("serial")
public class Proxy extends JLabel{
	public static LinkedList<String> forbiddenAddresses;
	public static Cache cache;
	public Thread thread;
	public ServerSocket welcomeSocket = null;
	public static boolean running = false;
	public Proxy() throws IOException {
		new Window(800,600,"471",this);
		
		forbiddenAddresses = new LinkedList<String>();
		cache = new Cache();
		
		forbiddenAddresses.add("www.yandex.com.tr");
		forbiddenAddresses.add("www.apple.com");
		forbiddenAddresses.add("www.facebook.com");
		start();
	}
	
	public void start() {
		running = true;
		thread = new Thread() {
			@Override
			public void run() {
				try {
					welcomeSocket = new ServerSocket(8080);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				while (true) {
					Socket connectionSocket = null;
					while(!running) {System.out.print("");}
					try {
						connectionSocket = welcomeSocket.accept();
					} catch (IOException e) {
					}
					new ServerHandler(connectionSocket);
				}
			}
		};
		thread.start();
	}
	
	
	public static void main(String args[]) throws Exception
	{
		new Proxy();
	}
}

