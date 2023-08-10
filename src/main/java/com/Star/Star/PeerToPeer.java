package com.Star.Star;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Peer to peer code for blockchain
 */
public abstract class PeerToPeer {
	private String ip;
	private int port;
	private ServerAddress peer;
	private ServerSocket serverSocket;
	private Socket sendSocket;
	private ConcurrentHashMap<String, TCPPackage> toSend = new ConcurrentHashMap<String, TCPPackage>();
	private boolean close = false;

	public PeerToPeer(String ip, int port, ServerAddress peer) throws Exception {
		this.ip = ip;
		this.port = port;
		this.peer = peer;
		this.serverSocket = new ServerSocket(port);

		Thread recv = new Thread(new Runnable() {
			public void run() {
				try {
					start();
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		});
		recv.start();
	}

	public void connectToPeer() {
		try {
			this.sendSocket = new Socket(this.peer.getIp(), this.peer.getPort());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		Thread sendLoop = new Thread(new Runnable() {
			public void run() {
				try {
					loopSend();
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		});
		sendLoop.start();
	}

	public void addToSend(Object msg) {
		TCPPackage tcpPack = null;
		try {
			tcpPack = new TCPPackage(new ServerAddress(this.ip, this.port), (TransactionPackage) msg);
		} catch (Exception e) {
			try {
				tcpPack = new TCPPackage(new ServerAddress(this.ip, this.port), (Block) msg);
			} catch (Exception e1) {
				System.err.println(e.getMessage());
			}
		}
		this.toSend.put(tcpPack.getHash(), tcpPack);
	}

	public void loopSend() throws InterruptedException {
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		try {
			out = new ObjectOutputStream(sendSocket.getOutputStream());
			in = new ObjectInputStream(sendSocket.getInputStream());
		} catch (Exception e) {
			loopSend();
			return;
		}

		while (!this.close) {
			if (this.toSend.size() > 5000) {
				Iterator<Entry<String, TCPPackage>> tcpIterator = this.toSend.entrySet().iterator();
				while (tcpIterator.hasNext()) {
					try {
						TCPPackage tcpPack = tcpIterator.next().getValue();
						out.writeObject(tcpPack);
						String hash = (String) in.readObject();
						this.toSend.remove(hash);
					} catch (Exception e) {
						System.err.println(e.getMessage());
					}
				}
			}
		}
		try {
			out.close();
			in.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public void start() throws IOException {
		new Receive(this.serverSocket.accept()).start();
	}

	public abstract void onRecieveMessage(Object msg) throws Exception;

	public class Receive extends Thread {
		private TCPPackage tcpPack = null;
		private final Socket clientSocket;

		public Receive(Socket socket) {
			clientSocket = socket;
		}

		public void run() {
			try {
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
				while (!close) {
					Object objReceived = in.readObject();
					tcpPack = (TCPPackage) objReceived;
					onRecieveMessage(tcpPack.getObject());
					String hash = tcpPack.getHash();
					out.writeObject(hash);
				}
				in.close();
				out.close();
				clientSocket.close();
				onRecieveMessage(tcpPack.getObject());
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
