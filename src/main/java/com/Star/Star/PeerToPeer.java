package com.Star.Star;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public abstract class PeerToPeer implements Runnable {
    private ServerSocket clientSocket;
    private ObjectInputStream in;
    
    public PeerToPeer(String ip, int port) throws UnknownHostException, IOException {
        clientSocket = new ServerSocket(port);
    }

    public void sendMessage(Object msg, String to_ip, int to_port) throws IOException {
    	ObjectOutputStream outSocket = new ObjectOutputStream(new Socket(to_ip, to_port).getOutputStream());
    	outSocket.writeObject(msg);
    	outSocket.flush();
    	outSocket.close();
    }

    public void recieveMessage() throws IOException, ClassNotFoundException {
    	while (!clientSocket.isClosed()) {
        	clientSocket.setSoTimeout(1000);
    		ObjectInputStream in = new ObjectInputStream(clientSocket.accept().getInputStream());
        	Object msg = in.readObject();
        	onRecieveMessage(msg);
        }
    	System.out.println("Quit Recieving");
    }

    public abstract void onRecieveMessage(Object msg);
    
    public void close() throws IOException {
        clientSocket.close();
    }

	public void run() {
		try {
			recieveMessage();
		} catch (Exception e) {}
	}
}
