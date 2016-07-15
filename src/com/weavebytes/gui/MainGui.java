package com.weavebytes.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.io.File;

import com.alee.laf.DefaultLayoutStyle;
import com.alee.laf.WebLookAndFeel;
import com.weavebytes.config.Config;
import com.weavebytes.services.ReceiveFileThread;
import com.weavebytes.services.SendFileThread;
import com.weavebytes.utils.Utils;

import javafx.collections.ListChangeListener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Main GUI class of IP Messenger
 * 
 * @author weavebytes
 *
 */
public class MainGui extends JFrame implements WindowListener, ActionListener, Runnable, ListSelectionListener, KeyListener{

	/**
	 * UI components
	 */
	private JTextArea                  tfSendMsg;
	private JTextArea                   taMsgs;
	private JList 						userList;
	private JFrame						waitDialog;

	/**
	 * model to store string name of all hosts, 
	 * to be shown on list in left side
	 */
	private DefaultListModel model;  

	/**
	 * IP of this user
	 */
	private String 	myIP;

	/**
	 * host of this user
	 */
	private String 	myHost;

	/**
	 * thread for receiving UDP messages
	 */
	private Thread thrdMsgReceiver;

	/**
	 * flag to stop thread for receiving UDP messages
	 */
	private boolean	stopped = false;

	/**
	 * hashtable to store a <host,ip> combination
	 */
	private Hashtable <String, String>  htblUsers = new Hashtable <String, String>();

	/**
	 * hashtable to store a <host, message-list> combination
	 */
	private Hashtable <String, Vector<String>>  htblMessages = new Hashtable <String, Vector<String>>();

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

		//---------------------------------------------------------
		// TOP
		//---------------------------------------------------------
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);

		JButton btnRefresh = new JButton("Refresh");
		btnRefresh.setActionCommand("Refresh");
		btnRefresh.addActionListener(this);
		
		
		//---------------------------------------------------------
		// CENTER
		//---------------------------------------------------------
		JPanel pnlCenter = new JPanel();
		pnlCenter.setLayout(new BorderLayout(5,5));

		JPanel pnlCenterBottom = new JPanel();
		
		pnlCenterBottom.setLayout(new BorderLayout(5, 5));

		JButton btnSend = new JButton("Send");
		btnSend.setActionCommand("Send");
		btnSend.setMaximumSize(new Dimension(100, 30));
		btnSend.addActionListener(this);

		tfSendMsg = new JTextArea();
		tfSendMsg.setPreferredSize(new Dimension(200, 100));
		
		taMsgs    = new JTextArea();
				
		tfSendMsg.addKeyListener(this);
		
		JScrollPane msgsScrollPane = new JScrollPane(taMsgs);
				
		pnlCenterBottom.add(tfSendMsg, BorderLayout.CENTER);
		pnlCenterBottom.add(btnSend,   BorderLayout.EAST);
		pnlCenter.add(msgsScrollPane,  BorderLayout.CENTER);
		pnlCenter.add(pnlCenterBottom, BorderLayout.SOUTH);

		model    = new DefaultListModel();
		userList = new JList(model);
		userList.addListSelectionListener(this);

		JScrollPane userListScrollPane = new JScrollPane(userList);
		
		//---------------------------------------------------------
		// BOTTOM
		//---------------------------------------------------------
		JLabel statusbar = new JLabel(" Statusbar");
		
		//---------------------------------------------------------
		// RIGHT
		//---------------------------------------------------------
		JPanel pnlRight = new JPanel();
		pnlRight.setLayout(new GridLayout(20, 1, 5, 10));
		JButton btnSendFile = new JButton("Send File");
		btnSendFile.setActionCommand("Send File");
		btnSendFile.addActionListener(this);
		pnlRight.add(btnSendFile);
		btnRefresh.setLocation(4,1);
		
		pnlRight.add(btnRefresh);


		// Adding GUI components
		add(toolbar,            BorderLayout.NORTH);
		add(userListScrollPane, BorderLayout.WEST);
		add(pnlCenter,          BorderLayout.CENTER);
		add(pnlRight, 	            BorderLayout.EAST);
		add(statusbar,          BorderLayout.SOUTH);
		pack();

		JMenuBar menuBar = new JMenuBar();
		
		 // File Menu, F - Mnemonic
	    JMenu fileMenu = new JMenu("File");
	    fileMenu.setMnemonic(KeyEvent.VK_F);
	    
	    JMenu settingsMenu = new JMenu("Settings");
	    settingsMenu.setMnemonic(KeyEvent.VK_S);
	    JMenu helpMenu = new JMenu("Help");	    	   
	    JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_Q);	 
	    
	    exit.addActionListener(new ActionListener() {
	    	
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(-1);
				
			}
		});   
	    
	    tfSendMsg.addKeyListener(new KeyAdapter() {
	    	int size = 40;
	    	int start = 0;
	    	@Override
	    	public void keyTyped(KeyEvent e) {
	    		
	    		if(tfSendMsg.getText().length() == size){
	    			tfSendMsg.append("\n" + "start : " + start + " end : " +size + tfSendMsg.getText().substring(start, size));
	    			start = size;
	    			size += 40;
	    			
	    		}
	    	}
		});
	   
	    //About dialog frame
	    final JFrame aboutDialog = new JFrame("About Text Messenger..");
	    aboutDialog.setSize(450, 600);  
	    aboutDialog.setLocation(200, 100);
	    
	    JLabel aboutText = new JLabel();
	    aboutText.setText("<html>IP Messenger IP Messenger IP Messenger <br> IP Messenger IP Messenger IP Messenger <br> IP Messenger IP Messenger</html>");
	    aboutText.setSize(350, 600);
	    
	    aboutDialog.getContentPane().add(aboutText);
	    
	    JMenuItem hostSettings = new JMenuItem("Host Settings", KeyEvent.VK_H);	  
	    
	    //setting dialog frame
	    final JFrame settings = new JFrame("Messenger Settings");
	    settings.setSize(350, 500);  
	    settings.setLocation(250, 100);
	    
	    hostSettings.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				settings.setVisible(true);				
			}
		});
		    
	    //Menu about dialog 
	    JMenuItem about = new JMenuItem("About");
	    
	    //actionListener for Menu about
	    about.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			aboutDialog.setVisible(true);
			}
		});
	    
	    fileMenu.add(exit);	    
	    settingsMenu.add(hostSettings);	    
	    helpMenu.add(about);
	    	    
	    menuBar.add(fileMenu);
	    menuBar.add(settingsMenu);
	    menuBar.add(helpMenu);
	    
	    this.setJMenuBar(menuBar);
		
		// Adding GUI components
		add(toolbar,            BorderLayout.NORTH);
		add(userListScrollPane, BorderLayout.WEST);
		add(pnlCenter,          BorderLayout.CENTER);
		add(pnlRight, 	            BorderLayout.EAST);
		add(statusbar,          BorderLayout.SOUTH);
		pack();

		// Size & Visibility
		setSize(720, 640);
		setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		setVisible(true);
			
	}

	/**
	 ********************************************************************
	 * 
	 *                       MAIN 
	 *
	 *********************************************************************                   
	 */
	public static void main(String[] args) {
		
		/*
		// You should work with UI (including installing L&F) inside Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater ( new Runnable ()
        {
            public void run ()
            {
                // Install WebLaF as application L&F
                WebLookAndFeel.install ();

                // You can also do that with one of old-fashioned ways:
                // UIManager.setLookAndFeel ( new WebLookAndFeel () );
                // UIManager.setLookAndFeel ( "com.alee.laf.WebLookAndFeel" );
                // UIManager.setLookAndFeel ( WebLookAndFeel.class.getCanonicalName () );

                // Create you application here using Swing components
                // JFrame frame = ...

                // Or use similar Web* components to get access to some extended features
                // WebFrame frame = ...
            }
        } );
		*/
		new MainGui();
	}
	
	

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
	public void windowOpened(java.awt.event.WindowEvent e) {}
	@Override
	public void windowClosed(java.awt.event.WindowEvent e) {}
	@Override
	public void windowIconified(java.awt.event.WindowEvent e) {}
	@Override
	public void windowDeiconified(java.awt.event.WindowEvent e) {}
	@Override
	public void windowActivated(java.awt.event.WindowEvent e) {}
	@Override
	public void windowDeactivated(java.awt.event.WindowEvent e) {}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("Pressed: " + e.getActionCommand());
		if(e.getActionCommand().equals("Send")) {
			sendClicked();	
		}
		if(e.getActionCommand().equals("Refresh")) {
			refreshClicked();	
		}
		
		if(e.getActionCommand().equals("Send File")) {
			sendFile();	
		}
	}
	
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub			
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub		
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			System.out.println("Enter key Pressed");
			sendClicked();
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
					System.out.println("Got msg ::: " + s);
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
		if(msg.startsWith("TCM")) processTCM(msg.substring(3));	
		if(msg.startsWith("SFR")) processSFR(msg.substring(3));	
		if(msg.startsWith("GMF")) processGMF(msg.substring(3));	
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

		if(!htblUsers.containsKey(otherHost)) {
			htblUsers.put(otherHost, otherIP);
			model.addElement(otherHost);
			
			// by default select the first one, when list has only one user
			if(model.size() == 1) userList.setSelectedIndex(0);
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

		if(!htblUsers.containsKey(otherHost)) {
			htblUsers.put(otherHost, otherIP);
			model.addElement(otherHost);
			
			// by default select the first one, when list has only one user
			if(model.size() == 1) userList.setSelectedIndex(0);
		}
	}

	/**
	 * function process a "Text Chat Message"
	 * 
	 * @param msg
	 */
	private void processTCM(String msg) {
		System.out.println("Got TCM=" + msg);

		String l[] = msg.split("\\:");

		String otherHost = l[0];
		String otherMsg = l[1];

		if(!htblMessages.containsKey(otherHost)) {
			Vector<String> vctMsgList = new Vector<String>();
			htblMessages.put(otherHost, vctMsgList);
		}

		Vector<String> vct = htblMessages.get(otherHost);
		vct.addElement(otherHost + ": " + otherMsg);
		if(userList.getSelectedValue().equals(otherHost)) taMsgs.append(otherHost + ": " + otherMsg + "\n");
	}
	
	
	/**
	 * other party send "SFR - Send File Request"
	 * asking me to receive the file,
	 * if I click yes, then other party should give me the file
	 * so, send GMF request to other party
	 * and start TCP thread to receive file 
	 * @param msg
	 */
	private void processSFR(String msg) {
	    
		String l[] = msg.split("\\::");
		
		String otherHost = l[0];
		String filePath = l[1];		
		int fileSize = Integer.parseInt(l[2]);
		System.out.println("File length: " + fileSize);
		
		String otherIP = htblUsers.get(otherHost);
			
		Path p = Paths.get(filePath);
	    String fileName = p.getFileName().toString();
	
	    //System.out.println("filePath = " + filePath);
	    //System.out.println("fileName = " + fileName);
	      
		JDialog.setDefaultLookAndFeelDecorated(true);
	    int response = JOptionPane.showConfirmDialog(null, 
	    		"Do you want to receive file from " + otherHost + " ?",
	    		"Receive File: " + fileName,
	            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
	    
	    if (response == JOptionPane.NO_OPTION) {
	      System.out.println("No button clicked");
	      Utils.sendUdpMsg("GMF" + myHost + "::" + "" + "::" + "No file", otherIP, Config.UDP_PORT);
	    } else if (response == JOptionPane.YES_OPTION) {
	    
	    System.out.println("Yes button clicked");  
	    
	 // parent component of the dialog
	    JFrame parentFrame = new JFrame();
	     
	    JFileChooser fileChooser = new JFileChooser();
	    fileChooser.setDialogTitle("Specify a file to save");   

	    fileChooser.setSelectedFile(new File(System.getProperty("user.home") + File.separator  + fileName) ); 
	    int userSelection = fileChooser.showSaveDialog(parentFrame);
	     
	    if (userSelection == JFileChooser.APPROVE_OPTION) {
	        File fileToSave = fileChooser.getSelectedFile();
	        
	        
	        
	        String savePath = fileToSave.getAbsolutePath();
	        System.out.println("Save as file: " + savePath);
	        
	        
	        Config.TCP_PORT++;
	        System.out.println("TCP Port : " +  Config.TCP_PORT );
		  	
		    new ReceiveFileThread(Config.TCP_PORT, savePath, fileSize).start();
		    Utils.sendUdpMsg("GMF" + myHost + "::" + Config.TCP_PORT + "::" + filePath, otherIP, Config.UDP_PORT);
	    }
	   
	         
	    } else if (response == JOptionPane.CLOSED_OPTION) {
	      System.out.println("JOptionPane closed");
	    }
	}
	
	/**
	 * after "SFR - Send File Request" is accepted, 
	 * a "GMF - Give Me File" is sent to other party
	 * to send the file.
	 *  
	 * @param msg
	 */
	private void processGMF(String msg) {
		
		try {
			this.waitDialog.setVisible(false);
		System.out.println("processing GMF");
		
		String l[] = msg.split("::");
		String filePath = l[2];
		String otherHost = l[0];
		//check for file
		if(!filePath.equals("No file"))
		{		
		int    port     = Integer.parseInt(l[1]);
		String otherIP = htblUsers.get(otherHost);
			
		Path p = Paths.get(filePath);
	    String fileName = p.getFileName().toString();
	    
	    System.out.println(String.format("+++ got otherHost=%s, filePath=%s, ip=%d, otherIP=%s", otherHost, filePath, port, otherIP));
	    	     
	    new SendFileThread(filePath, otherIP, port).start();
		}
		else{
				JOptionPane.showMessageDialog(null,  otherHost + " : Refused to accept the file");
		 }
		} catch(Exception e) {
			System.out.println("processGMF exception ::" + e);
			e.printStackTrace();
		}
	}

	/**
	 * function sends a chat message to selected user on left side
	 */
	private void sendClicked() {
		String otherHost = userList.getSelectedValue().toString();

		String otherIP = htblUsers.get(otherHost );

		String msg = tfSendMsg.getText();

		Utils.sendUdpMsg("TCM" + myHost + ":" + msg, otherIP, Config.UDP_PORT);	

		if(!htblMessages.containsKey(otherHost)) {
			Vector<String> vctMsgList = new Vector<String>();
			htblMessages.put(otherHost, vctMsgList);
		}

		Vector<String> vct = htblMessages.get(otherHost);
		vct.addElement(myHost + ": " + msg);
		taMsgs.append(myHost + ": " + msg + "\n");

		tfSendMsg.setText("");
	}
	
	public JFrame showWaitingDialog(String Otherhost, String filename){
		  JFrame sendFileWaitFrm = new JFrame("Waiting for reply");
		  sendFileWaitFrm.setBounds(500, 300, 300, 150);
          JPanel waitPanel = new JPanel(new GridLayout(5, 1, 5, 5));
          JLabel waitLabel = new JLabel();
          JLabel fileNameLabel = new JLabel();
          JLabel hostNameLabel = new JLabel();
          waitLabel.setText("               Waiting for receiving file  ");
          
          hostNameLabel.setText("        Host Name               :         " + Otherhost);
          fileNameLabel.setText("        File Name                 :         " + filename );
          
          waitPanel.add(new JPanel());
          waitPanel.add(waitLabel);
          waitPanel.add(hostNameLabel);
          waitPanel.add(fileNameLabel);
          sendFileWaitFrm.add(waitPanel);
          sendFileWaitFrm.setVisible(true);         
          return sendFileWaitFrm;
	}
	
	/**
	 * function sends a "SFR - Send File Request" to other user
	 */
	private void sendFile() {
		
		String otherHost = userList.getSelectedValue().toString();
		String otherIP = htblUsers.get(otherHost );
		
		JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
                  
        if (returnValue == JFileChooser.APPROVE_OPTION) {
           File selectedFile = fileChooser.getSelectedFile();
           
           System.out.println(selectedFile.getName());
           System.out.println("getCurrentDirectory(): " + fileChooser.getCurrentDirectory());
           System.out.println("getSelectedFile() : " + fileChooser.getSelectedFile());
           
           String fullPath = fileChooser.getSelectedFile().toString();
                      
           Utils.sendUdpMsg("SFR" + myHost + "::" + fullPath + "::" + new File(fullPath).length(), otherIP, Config.UDP_PORT);	
         this.waitDialog =  showWaitingDialog(otherHost, Paths.get(fullPath).getFileName().toString());
        }
	}

	/**
	 * function sends IAI to all users in n/w
	 * will cause user list to be refreshed on left side
	 * as each user will response with MTI
	 */
	private void refreshClicked() {
		System.out.println("handle refresh: " + Utils.getHost());
		Utils.sendUdpBroadcast("IAI" + myIP + ":" + myHost, Config.UDP_PORT);
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

	@Override
	public void valueChanged(ListSelectionEvent e) {
		System.out.println("valueChanged: " + userList.getSelectedValue());

		taMsgs.setText("");

		String otherHost = userList.getSelectedValue().toString();

		if(!htblMessages.containsKey(otherHost) ) return;

		Vector<String> msgList = htblMessages.get(otherHost);
		for(int i=0; i < msgList.size(); i++) {

			String msg = msgList.elementAt(i).toString();
			taMsgs.setText(taMsgs.getText() + msg + "\n");
		}	
	}

}//MainGui
