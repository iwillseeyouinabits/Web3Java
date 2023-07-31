package com.Star.Star;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public abstract class PeerToPeer implements Runnable {
	private ServerSocket[] clientSocket;
	private ObjectInputStream in;
	private String ip;
	private int portStart;
	private int portEnd;
	private int maxSendAgain;
	private int numSendAgain = 0;

	public PeerToPeer(String ip, int portStart, int numPorts) throws Exception {
		clientSocket = new ServerSocket[numPorts];
		for (int i = 0; i < numPorts; i++) {
			clientSocket[i] = new ServerSocket(portStart + i);
		}
		this.ip = ip;
		this.portStart = portStart;
		this.portEnd = portStart + numPorts;
		maxSendAgain = numPorts;
		recieveMessage();
	}

	public void sendMessage(Object msg, String to_ip, int to_port) throws IOException {
		try {
			if ((!to_ip.equals(this.ip) || (to_port < this.portStart || to_port > this.portEnd))) {
				OutputStream os = new Socket(to_ip, to_port).getOutputStream();
				ObjectOutputStream outSocket = new ObjectOutputStream(os);
				outSocket.writeObject(msg);
				outSocket.flush();
				outSocket.close();
			}
		} catch (Exception e) {
			if (numSendAgain < maxSendAgain) {
				numSendAgain++;
				sendMessage(msg, to_ip, to_port + 1);
			} else {
				this.numSendAgain = 0;
			}
		}
	}

	public void recieveMessage() throws Exception {
		for (int i = 0; i < this.portEnd - this.portStart; i++) {
			final int index = i;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						recieveMessageHelper(index);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			t.start();
		}
	}

	public void recieveMessageHelper(int index) throws Exception {
		while (true) {
			Socket s = clientSocket[index].accept();
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			try {
				Object msg = in.readObject();
				onRecieveMessage(msg);
			} catch (Exception e) {
				s.close();
				in.close();
				e.printStackTrace();
			}
		}
	}

	public abstract void onRecieveMessage(Object msg) throws Exception;

	public void close() throws IOException {
		for (int i = 0; i < this.portEnd - this.portEnd; i++) {
			clientSocket[i].close();
		}
	}

	public void run() {
		try {
			recieveMessage();
		} catch (Exception e) {
		}
	}
}
