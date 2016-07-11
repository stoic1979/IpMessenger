package com.weavebytes.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowListener;

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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import javafx.geometry.Insets;

public class MainGui extends JFrame implements WindowListener{

	
	private JList userList;
	private DefaultListModel model;
    private int counter = 15;
	
	
	public MainGui() {
		super("IP Messenger");

	    addWindowListener(this);
	    
	    initGui();	
	}
	
	
	private void initGui() {
		
		setLayout(new BorderLayout(5,5));
		
		// top toolbar....................................
		JToolBar toolbar = new JToolBar();
	    toolbar.setFloatable(false);
	    
	    JButton btnRefresh = new JButton("Refresh");
	    toolbar.add(btnRefresh);
	   
	    // center panel....................................
	    JPanel pnlCenter = new JPanel();
	    pnlCenter.setLayout(new BorderLayout(5,5));
	   
	    JPanel pnlCenterBottom = new JPanel();
	    pnlCenterBottom.setLayout(new BorderLayout(5,5));
	    JButton btnSend = new JButton("Send");
	    JTextField tfSendMsg = new JTextField("send msg");
	    JTextArea taMsgs = new JTextArea("some messages");
	    JScrollPane msgsScrollPane = new JScrollPane(taMsgs);
	    
	    pnlCenterBottom.add(tfSendMsg, BorderLayout.CENTER);
	    pnlCenterBottom.add(btnSend, BorderLayout.EAST);
	    pnlCenter.add(msgsScrollPane, BorderLayout.CENTER);
	    pnlCenter.add(pnlCenterBottom, BorderLayout.SOUTH);
		
		model = new DefaultListModel();
		userList = new JList(model);
	    JScrollPane userListScrollPane = new JScrollPane(userList);
	    for (int i = 0; i < 150; i++)
	        model.addElement("Element " + i);
	    
	    // bottom...........................................
	    JLabel statusbar = new JLabel(" Statusbar");
	    
	    
	    // right............................................
	    JLabel right = new JLabel(" right side ");

	    
	    add(toolbar, BorderLayout.NORTH);
	    add(userListScrollPane, BorderLayout.WEST);
	    add(pnlCenter, BorderLayout.CENTER);
	    add(right, BorderLayout.EAST);
	    
	    add(statusbar, BorderLayout.SOUTH);
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
}//MainGui
