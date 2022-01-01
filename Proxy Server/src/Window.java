import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

@SuppressWarnings("serial")
public class Window extends JFrame implements ActionListener, Runnable{
	
	public Window(int width, int height,String title,Proxy proxy) 
	{
		setTitle(title);
		setMaximumSize(new Dimension(width,height));
	    setMinimumSize(new Dimension(width,height));
	    setResizable(false);
	    setLocationRelativeTo(null);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
	    add(proxy);
	    run();
	}
	
	public void createMenu() {
		/* Create Menu Bar */
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		/* Create Menu Items */
		JMenu file = new JMenu("File");
		menuBar.add(file);
		
			JMenuItem start = new JMenuItem("Start");
			file.add(start);
			start.setActionCommand("Start");
			start.addActionListener(this);
		
			JMenuItem stop = new JMenuItem("Stop");
			file.add(stop);
			stop.setActionCommand("Stop");
			stop.addActionListener(this);
			
			JMenuItem report = new JMenuItem("Report");
			file.add(report);
			report.setActionCommand("Report");
			report.addActionListener(this);
			
			JMenuItem filter = new JMenuItem("Add host to filter");
			file.add(filter);
			filter.setActionCommand("Filter");
			filter.addActionListener(this);
			
			JMenuItem display_filter = new JMenuItem("Display current filtered hosts");
			file.add(display_filter);
			display_filter.setActionCommand("Display");
			display_filter.addActionListener(this);
			
			JMenuItem exit = new JMenuItem("Exit");
			file.add(exit);
			exit.setActionCommand("Exit");
			exit.addActionListener(this);
		
		JMenu help = new JMenu("Help");
		menuBar.add(help);
		help.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				getContentPane().removeAll();
				JLabel labelM = new JLabel("Kamran Musayev", SwingConstants.CENTER);
				labelM.setFont(new Font("Verdana", Font.BOLD | Font.ITALIC, 22));
				add(labelM);
				revalidate();
				repaint();		
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
	} 
	

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if(command.equals("Start")) {
			Proxy.running = true;
			getContentPane().removeAll();
			JLabel labelM = new JLabel("Proxy Server is Running...", SwingConstants.CENTER);
			labelM.setFont(new Font("Verdana", Font.BOLD | Font.ITALIC, 22));
			add(labelM);
			revalidate();
			repaint();
		}
		else if(command.equals("Stop")) {
			Proxy.running = false;
			getContentPane().removeAll();
			JLabel labelM = new JLabel("Proxy Server is Down...", SwingConstants.CENTER);
			labelM.setFont(new Font("Verdana", Font.BOLD | Font.ITALIC, 22));
			add(labelM);
			revalidate();
			repaint();
		}
		else if(command.equals("Report")) /* Add report */ ;
		else if(command.equals("Filter")) {
			String host = JOptionPane.showInputDialog("Add host to filter");
			Proxy.forbiddenAddresses.add(host);
			getContentPane().removeAll();
			@SuppressWarnings("rawtypes")
			JList list = new JList(Proxy.forbiddenAddresses.toArray());
			add(list);
			revalidate();
			repaint();
		}
		else if(command.equals("Display")) {
			getContentPane().removeAll();
			@SuppressWarnings("rawtypes")
			JList list = new JList(Proxy.forbiddenAddresses.toArray());
			add(list);
			revalidate();
			repaint();
		}	
		else if(command.equals("Exit")) System.exit(0);
		
	}

	@Override
	public void run() {
	    createMenu();
	    setVisible(true);
	}
}
