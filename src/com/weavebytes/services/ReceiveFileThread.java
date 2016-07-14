package com.weavebytes.services;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.weavebytes.config.Config;


public class ReceiveFileThread  extends Thread {

	private String filePath;
    private int port;
	/**
	 * constructor
	 * 
	 * @param filePath
	 */
	public ReceiveFileThread(int port, String filePath) {
		this.filePath = filePath;
		this.port = port;
	}

	
	/**
	 * thread run method
	 */
	public void run() {
		
		System.out.println("[ReceiveFileThread] :: started on port " + port);

		try {
			
			ServerSocket ssock = new ServerSocket(port);			
			Socket socket = ssock.accept();
			
			byte[] contents = new byte[10000];

			//Initialize the FileOutputStream to the output file's full path.
			FileOutputStream fos = new FileOutputStream(filePath);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			InputStream is = socket.getInputStream();

			//No of bytes read in one read() call
			int bytesRead = 0; 

			//receiving file Message dialog window
			JFrame recFileMessFrame =  new JFrame("Recieving File");
			recFileMessFrame.setBounds(500, 300, 350, 200);
		  	
			
		  	JPanel panel = new JPanel();
		  	panel.setLayout(new GridLayout(9, 1));
		  	
		    for(int i = 0; i <= 2; i++){
			    panel.add(new JPanel());
			}
		    //file recieving message panel
			JPanel recievingMessPanel = new JPanel(new BorderLayout(5, 5));
			JLabel waitLabel = new JLabel();
			waitLabel.setText("Please wait... recieving file");
			recievingMessPanel.add(waitLabel);
			recievingMessPanel.setBackground(Color.white);
			recFileMessFrame.add(recievingMessPanel);
			recFileMessFrame.setVisible(true);
			while((bytesRead=is.read(contents))!=-1){
				bos.write(contents, 0, bytesRead); 
			}
			bos.flush();
			recFileMessFrame.setVisible(false);
			socket.close();
			ssock.close();
		} catch(Exception e) {
			System.out.println("[ReceiveFileThread] exception :: " + e);
			e.printStackTrace();
		}

		System.out.println("[ReceiveFileThread] :: File saved successfully!");
	}

}//ReceiveFileThread
