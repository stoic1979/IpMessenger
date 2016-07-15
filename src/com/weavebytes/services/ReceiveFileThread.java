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
import java.nio.file.Paths;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.weavebytes.config.Config;


public class ReceiveFileThread  extends Thread {

	private String filePath;
    private int port;
    private long fileSize;
	/**
	 * constructor
	 * 
	 * @param filePath
	 */
	public ReceiveFileThread(int port, String filePath, int fileSize) {
		this.filePath = filePath;
		this.port = port;
		this.fileSize = (long)fileSize; 
	}

	
	/**
	 * thread run method
	 */
	public void run() {
		
		System.out.println("[ReceiveFileThread] :: started on port " + port + "file size : " + fileSize);
        int progress = 0;
		try {
			
			ServerSocket ssock = new ServerSocket(port);			
			Socket socket = ssock.accept();
			int size = 10000;
			byte[] contents = new byte[size];

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
		  	
		 	//progress bar for recieved data
		  	JProgressBar progressbar = new JProgressBar(0, 100); 	
		  	progressbar.setSize(new Dimension(100, 15));
		    progressbar.setBackground(Color.white);
		    progressbar.setForeground(Color.gray);		    
		   
		    //adding blank frame
		    for(int i = 0; i <= 2; i++){
			    panel.add(new JPanel());
			}
		    
		    JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
			JLabel progressLabel = new JLabel();
			
			//adding progress label to progress panel
			progressPanel.add(progressLabel);			
			panel.add(progressPanel);
			
		    //adding progress panel to main panel
			panel.add(progressbar);
		
			recFileMessFrame.add(panel);
			recFileMessFrame.setVisible(true);
			long recFileSize = 0;
			
		    while((bytesRead=is.read(contents))!=-1){
		    			   	
		    	recFileSize += bytesRead; 
		    	//change the progressbar value
				progressbar.setValue((progress = (int)((recFileSize*100)/fileSize)));
				progressLabel.setText("File Recieved......." + progress + "%");		
				bos.write(contents, 0, bytesRead);
			}
			bos.flush();
			recFileMessFrame.setVisible(false);
			JOptionPane.showMessageDialog(null, "File recieved ! " + Paths.get(filePath).getFileName() );
			socket.close();
			ssock.close();
		} catch(Exception e) {
			System.out.println("[ReceiveFileThread] exception :: " + e);
			e.printStackTrace();
		}

		System.out.println("[ReceiveFileThread] :: File saved successfully!");
	}

}//ReceiveFileThread
