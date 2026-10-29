package com.limewire.translations;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class StreamConsumer {

	private final BufferedReader bufferedReader;
	
	private Thread thread;
	
	public StreamConsumer(InputStream inputStream, String fileName) {
		bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		PrintWriter writer = null;
//		thread = new Thread(new Runnable() {
//			public void run() {
		String line = null;
		try {
			writer = new PrintWriter(new FileWriter("/tmp/" + fileName));
			while ((line = bufferedReader.readLine()) != null) {
				writer.println(line);
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException e) {
			}
			if (writer != null) {
				writer.close();
			} 
		}
//		}
//		});
//		thread.setDaemon(false);
	}

	public void start() {
		thread.start();
	}
	
}
