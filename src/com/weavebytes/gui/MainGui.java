package com.weavebytes.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Hashtable;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import com.weavebytes.config.Config;
import com.weavebytes.utils.Utils;

/**
 * Main GUI class of IP Messenger
 * 
 * @author weavebytes
 *
 */
public class MainGui extends JFrame implements WindowListener, ActionListener, Runnable{

	
	private JList 						userList;
	private DefaultListModel 			model;  
    private String 						myIP;
    private String 						myHost;
    private boolean 					stopped = false;
    private Thread 						thrdMsgReceiver;
	private Hashtable <String, String>  htblUsers = new Hashtable <String, String>();
	
    /**
     * constructor
     */
	public MainGui() {
		super("IP Messenger");

	    addWindowListener(this);
	    
	    initGui();	
	    initMessenger();
	}
	
	/**
	 * function initializes various GUI components
	 */
	private void initGui() {
		
		setLayout(new BorderLayout(5,5));
		
		// top toolbar....................................
		JToolBar toolbar = new JToolBar();
	    toolbar.setFloatable(false);
	    
	    JButton btnRefresh = new JButton("Refresh");
	    btnRefresh.setActionCommand("Refresh");
	    btnRefresh.addActionListener(this);
	    toolbar.add(btnRefresh);
	   
	    // center panel....................................
	    JPanel pnlCenter = new JPanel();
	    pnlCenter.setLayout(new BorderLayout(5,5));
	   
	    JPanel pnlCenterBottom = new JPanel();
	    pnlCenterBottom.setLayout(new BorderLayout(5,5));
	    
	    JButton btnSend = new JButton("Send");
	    btnSend.setActionCommand("Send");
	    btnSend.addActionListener(this);
	    
	    JTextField tfSendMsg       = new JTextField("send msg");
	    JTextArea taMsgs           = new JTextArea("some messages");
	    JScrollPane msgsScrollPane = new JScrollPane(taMsgs);
	    
	    pnlCenterBottom.add(tfSendMsg, BorderLayout.CENTER);
	    pnlCenterBottom.add(btnSend,   BorderLayout.EAST);
	    pnlCenter.add(msgsScrollPane,  BorderLayout.CENTER);
	    pnlCenter.add(pnlCenterBottom, BorderLayout.SOUTH);
		
		model    = new DefaultListModel();
		userList = new JList(model);
	    JScrollPane userListScrollPane = new JScrollPane(userList);
    
	    // bottom...........................................
	    JLabel statusbar = new JLabel(" Statusbar");
	    
	    
	    // right............................................
	    JLabel right = new JLabel(" right side ");

	    
	    add(toolbar,            BorderLayout.NORTH);
	    add(userListScrollPane, BorderLayout.WEST);
	    add(pnlCenter,          BorderLayout.CENTER);
	    add(right, 	            BorderLayout.EAST);
	    add(statusbar,          BorderLayout.SOUTH);
	    pack();
	    
	    setSize(720, 640);
	    setVisible(true);
	    
	}
	
	
	public static void main(String[] args) {
	  new MainGui();

	}


	@Override
	public void windowOpened(java.awt.event.WindowEvent e) {}


	@Override
	public void windowClosing(java.awt.event.WindowEvent e) {
		/*if (JOptionPane.showConfirmDialog(this, 
				"Are you sure you want to quit?", 
				"Confirm exit.", 
				JOptionPane.OK_OPTION, 0, new ImageIcon("")) != 0) {
            return;
        }*/
        System.exit(-1);
		
	}


	@Override
	public void windowClosed(java.awt.event.WindowEvent e) {}


	@Override
	public void windowIconified(java.awt.event.WindowEvent e) {}


	@Override
	public void windowDeiconified(java.awt.event.WindowEvent e) {}


	@Override
	public void windowActivated(java.awt.event.WindowEvent e) {}


	@Override
	public void windowDeactivated(java.awt.event.WindowEvent e) {
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("Pressed: " + e.getActionCommand());
		if(e.getActionCommand().equals("Send")) {
			sendClicked();	
		}
		if(e.getActionCommand().equals("Refresh")) {
			refreshClicked();	
		}
		
	}
	
	/**
	 * thread for listening all incoming UDP messages
	 * on port Config.UDP_PORT
	 */
	public void run() {
	    byte[] buffer = new byte[65507];
	    DatagramSocket socket;
	    try {
	     socket = new DatagramSocket(Config.UDP_PORT);
	   
	    
	    while (true) {
	      if (stopped)
	        return;
	      DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
	      try {
	        socket.receive(dp);
	        String s = new String(dp.getData(), 0, dp.getLength());
	        System.out.println("Got msg: " + s);
	        processMessage(s);
	        Thread.yield();
	      } catch (IOException ex) {
	        System.err.println(ex);
	      }
	    }//while
	    	} catch(Exception e) {
	    	
	    }
	  }
	/**
	 * function process messages received
	 * 
	 * @param msg
	 */
	private void processMessage(String msg) {
		if(msg.startsWith("IAI")) processIAI(msg.substring(3));	
		if(msg.startsWith("MTI")) processMTI(msg.substring(3));	
	}
	
	/**
	 * function process an "I Am In" message
	 * 
	 * @param msg
	 */
	private void processIAI(String msg) {
		System.out.println("Got IAI=" + msg);
		
		String l[] = msg.split("\\:");
	
		String otherIP   = l[0];
		String otherHost = l[1];	
		
		
		
		Utils.sendUdpMsg("MTI" + myIP + ":" + myHost , otherIP, Config.UDP_PORT);
		
		if(!htblUsers.contains(otherHost)) {
			htblUsers.put(otherHost, otherIP);
			model.addElement(otherHost);
		}
		
	}
	
	/**
	 * function process an "Me Too In" message
	 * 
	 * @param msg
	 */
	private void processMTI(String msg) {
		
		System.out.println("Got MTI=" + msg);
		
		String l[] = msg.split("\\:");
	
		String otherIP   = l[0];
		String otherHost = l[1];	
		
		if(!htblUsers.contains(otherHost)) {
			htblUsers.put(otherHost, otherIP);
			model.addElement(otherHost);
		}
	}
	
	
	private void sendClicked() {
		System.out.println("Sending message");
		Utils.sendUdpBroadcast("IAIhello", Config.UDP_PORT);
	}
	
	private void refreshClicked() {
		System.out.println("handle refresh: " + Utils.getHost());
	}
	
	/**
	 * function initializes the messenger with ip, host,
	 * UDP msg listener thread etc.
	 */
	private void initMessenger() {
		myIP = Utils.getIP();
		myHost = Utils.getHost();
		thrdMsgReceiver = new Thread(this);
		thrdMsgReceiver.start();
		Utils.sendUdpBroadcast("IAI" + myIP + ":" + myHost, Config.UDP_PORT);
	}
	
	
}//MainGui
