package com.weavebytes.services;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.weavebytes.config.Config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public class SendFileThread extends Thread {

	private String filePath;
	private String otherHost;

	/**
	 * constructor
	 * 
	 * @param filePath
	 */
	public SendFileThread(String filePath, String otherHost) {
		this.filePath = filePath;
		this.otherHost = otherHost;
	}

	/**
	 * thread run method
	 */
	public void run() {
		
		System.out.println("[SendFileThread] :: started... ");

		try {
			ServerSocket ssock = new ServerSocket(Config.TCP_PORT);
			Socket socket = ssock.accept();

			//The InetAddress specification
			InetAddress IA = InetAddress.getByName(otherHost); 

			//Specify the file
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis); 

			//Get socket's output stream
			OutputStream os = socket.getOutputStream();

			//Read File Contents into contents array 
			byte[] contents;
			long fileLength = file.length(); 
			long current = 0;

			long start = System.nanoTime();
			while(current!=fileLength){ 
				int size = 10000;
				if(fileLength - current >= size)
					current += size;    
				else{ 
					size = (int)(fileLength - current); 
					current = fileLength;
				} 
				contents = new byte[size]; 
				bis.read(contents, 0, size); 
				os.write(contents);
				System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!");
			}   

			os.flush(); 
			//File transfer done. Close the socket connection!
			socket.close();
			ssock.close();
		}catch(Exception e) {
			System.out.println("[SendFileThread] :: exception: " + e);
			e.printStackTrace();
		}

		System.out.println("[SendFileThread] :: File sent succesfully!");

	}

}//SendFileThread
