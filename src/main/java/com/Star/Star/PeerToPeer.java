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
				}
			}
		});
		recv.start();
	}

	public void connectToPeer() {
		try {
			this.sendSocket = new Socket(this.peer.getIp(), this.peer.getPort());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Thread sendLoop = new Thread(new Runnable() {
			public void run() {
				try {
					loopSend();
				} catch (Exception e) {
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
				e1.printStackTrace();
			}
		}
		this.toSend.put(tcpPack.getHash(), tcpPack);
//		System.out.println(toSend.size() + " " + port);
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
//						System.out.println("FAIL SEND");
						e.printStackTrace();
					}
				}
			}
		}
		try {
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() throws IOException {
		new Recieve(this.serverSocket.accept()).start();
	}

	public abstract void onRecieveMessage(Object msg) throws Exception;

	public class Recieve extends Thread {
		private TCPPackage tcpPack = null;
		private final Socket clientSocket;

		public Recieve(Socket socket) {
			clientSocket = socket;
		}

		public void run() {
			try {
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
				while (!close) {
					Object objRecieved = in.readObject();
					tcpPack = (TCPPackage) objRecieved;
					onRecieveMessage(tcpPack.getObject());
					String hash = tcpPack.getHash();
					out.writeObject(hash);
				}
				in.close();
				out.close();
				clientSocket.close();
//				System.out.println("recv");
				onRecieveMessage(tcpPack.getObject());
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
