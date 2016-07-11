package com.weavebytes.services;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

import com.weavebytes.config.Config;


public class ReceiveFileThread  extends Thread {

	private String filePath;

	/**
	 * constructor
	 * 
	 * @param filePath
	 */
	public ReceiveFileThread(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * thread run method
	 */
	public void run() {
		
		System.out.println("[ReceiveFileThread] :: started... ");

		try {
			Socket socket = new Socket(InetAddress.getByName("localhost"), Config.TCP_PORT);
			byte[] contents = new byte[10000];

			//Initialize the FileOutputStream to the output file's full path.
			FileOutputStream fos = new FileOutputStream(filePath);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			InputStream is = socket.getInputStream();

			//No of bytes read in one read() call
			int bytesRead = 0; 

			while((bytesRead=is.read(contents))!=-1)
				bos.write(contents, 0, bytesRead); 

			bos.flush(); 
			socket.close();
		} catch(Exception e) {
			System.out.println("[ReceiveFileThread] exception :: " + e);
			e.printStackTrace();
		}

		System.out.println("[ReceiveFileThread] :: File saved successfully!");
	}

}//ReceiveFileThread
