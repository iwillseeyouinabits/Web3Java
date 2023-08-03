package com.Star.Star;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class PeerToPeer implements Runnable {
	private ServerSocket clientSocket;
	private ObjectInputStream in;
	List<TransactionPackage> tpChunck;
	private int chunckSize;
	private String ip;
	private int port;
	Thread t;
	HashMap<String, TCPPackage> sentNotVerified = new HashMap<String, TCPPackage>();

	public PeerToPeer(String ip, int port, int chunckSize) throws Exception {
		clientSocket = new ServerSocket(port);
		this.chunckSize = chunckSize;
		tpChunck = Collections.synchronizedList(new ArrayList<TransactionPackage>());
		this.ip = ip;
		this.port = port;
		recieveMessage();
	}

	public void sendMessage(Object msg, List<ServerAddress> peers) throws IOException {
		ArrayList<TransactionPackage> tpChunckSend = null;
		try {
			TransactionPackage tp = (TransactionPackage) msg;
			tpChunck.add(tp);
			if (tpChunck.size() >= this.chunckSize) {
				tpChunckSend = new ArrayList<TransactionPackage>();
				for (int k = 0; k < tpChunck.size(); k++) {
					TransactionPackage tpSend = tpChunck.remove(0);
					tpChunckSend.add(new TransactionPackage(tpSend));
				}
				TCPPackage tcpPack = new TCPPackage(new ServerAddress(this.ip, this.port), tpChunckSend);
				this.sendTransaction(peers, tcpPack);
			}
		} catch (ClassCastException e) {
			try {
				Block block = (Block) msg;
				TCPPackage tcpPack = new TCPPackage(new ServerAddress(this.ip, this.port), block);
				this.sendTransaction(peers, tcpPack);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		} catch (ConnectException e2) {
			tpChunck.addAll(tpChunckSend);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	private void sendTransaction(List<ServerAddress> peers, TCPPackage tcpPack) throws IOException {
		for (int i = 0; i < peers.size(); i++) {
			if (!peers.get(i).getIp().equals(this.ip) || peers.get(i).getPort() != this.port) {
				OutputStream os = new Socket(peers.get(i).getIp(), peers.get(i).getPort()).getOutputStream();
				ObjectOutputStream outSocket = new ObjectOutputStream(os);
				outSocket.writeObject(tcpPack);
				outSocket.flush();
				outSocket.close();
			}
		}
	}

	public void recieveMessage() throws Exception {
		this.t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					recieveMessageHelper();
				} catch (Exception e) {
//					e.printStackTrace();
				}
			}
		});
		t.start();
	}

	public void recieveMessageHelper() throws Exception {
		while (true) {
			Socket s = clientSocket.accept();
			ObjectInputStream in = new ObjectInputStream(s.getInputStream());
			try {
				Object msg = in.readObject();
				TCPPackage tcpPack = (TCPPackage) msg;
				try {
					List<TransactionPackage> recv = (List<TransactionPackage>) tcpPack.getObject();
//					System.out.println("recieved");
					for (TransactionPackage tp : recv) {
						onRecieveMessage(tp);
					}
				} catch (Exception e) {
					onRecieveMessage((Block) tcpPack.getObject());
				}
			} catch (Exception e) {
				s.close();
				in.close();
				e.printStackTrace();
			}
		}
	}

	public void flush(List<ServerAddress> peers) throws Exception {
		ArrayList<TransactionPackage> tpChunckSend = new ArrayList<TransactionPackage>();
		while (!tpChunck.isEmpty()) {
			try {
				TransactionPackage tpSend = tpChunck.remove(0);
				tpChunckSend.add(new TransactionPackage(tpSend));
			} catch (Exception e) {

			}
		}
		try {
			if (tpChunckSend.size() > 0) {
				TCPPackage tcpPack = new TCPPackage(new ServerAddress(this.ip, this.port), tpChunckSend);
				this.sendTransaction(peers, tcpPack);
			}
		} catch (ConnectException e2) {
//			System.out.println("Failed To Send");
			tpChunck.addAll(tpChunckSend);

		}
	}

	public abstract void onRecieveMessage(Object msg) throws Exception;

	public void close() throws IOException {
		t.stop();
		clientSocket.close();
	}

	public void run() {
		try {
			recieveMessage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
