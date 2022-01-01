import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Printer {
	private static Queue<Queue<String>> handler;
	@SuppressWarnings("unused")
	private final static PrinterHelper pH = new PrinterHelper();
	private Queue<String> list;
	private static Object lock ,removeLock;
	
	public Printer() {
		handler = new LinkedBlockingQueue<Queue<String>>();
		lock = new Object();
		removeLock = new Object();
		list = new LinkedBlockingQueue<>();
		handler.add(list);
		synchronized (lock) {
			lock.notify();
		}
	}

	public void add(String str) {
		list.add(str);
		synchronized (lock) {
			lock.notify();
		}
	}

	public void removeThread() {
		while (!list.isEmpty()) {
			synchronized (removeLock) {
				try {
					removeLock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		handler.remove(list);
	}
	
	static class PrinterHelper extends Thread {
		
		PrinterHelper() {
			start();
		}

		@Override
		public void run() {
			while (true) {
				if(!Proxy.running) break;
				synchronized (lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				Queue<String> tmp = handler.element();
				while (!tmp.isEmpty()) System.out.println(tmp.remove());
				synchronized (removeLock) {
					removeLock.notifyAll();
				}

			}

		}

	}
}
