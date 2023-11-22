package com.Star.Star;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
	protected ConcurrentHashMap<String, TCPPackage> toSend = new ConcurrentHashMap<String, TCPPackage>();
	private List<TransactionPackage> tpChunck;
	private int maxTpChunckSize;
	private boolean close = false;

	public PeerToPeer(String ip, int port, ServerAddress peer, int maxTpChunckSize) throws Exception {
		this.ip = ip;
		this.port = port;
		this.peer = peer;
		this.serverSocket = new ServerSocket(port);
		this.tpChunck = Collections.synchronizedList(new ArrayList<TransactionPackage>());
		this.maxTpChunckSize = maxTpChunckSize;
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
			System.err.println(e.getMessage());
		}

		Thread sendLoop = new Thread(() -> {
            try {
                loopSend();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        });
		sendLoop.start();
	}

	public void addToSend(Object msg) {
		TCPPackage tcpPack = null;
		try {
			TransactionPackage tp = (TransactionPackage) msg;
			tcpPack = new TCPPackage(new ServerAddress(this.ip, this.port), tp);
			this.tpChunck = Collections.synchronizedList(new ArrayList<TransactionPackage>());
			this.toSend.put(tcpPack.getHash(), tcpPack);
		} catch (Exception e) {
			try {
				e.printStackTrace();
				tcpPack = new TCPPackage(new ServerAddress(this.ip, this.port), (Block) msg);
				this.toSend.put(tcpPack.getHash(), tcpPack);
			} catch (Exception e1) {
				System.err.println(e.getMessage());
			}
		}
//		System.out.println(toSend.size() + " " + port);
	}

	public void loopSend(){
		ObjectOutputStream out;
		ObjectInputStream in;
		try {
			out = new ObjectOutputStream(sendSocket.getOutputStream());
			in = new ObjectInputStream(sendSocket.getInputStream());
		} catch (Exception e) {
			loopSend();
			return;
		}

		while (!this.close) {
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
				System.out.println("Start recv");
				while (!close) {
					Object objRecieved = in.readObject();
					tcpPack = (TCPPackage) objRecieved;
					String hash = tcpPack.getHash();
					TransactionPackage tp = (TransactionPackage) tcpPack.getObject();
					onRecieveMessage(tp);
					out.writeObject(hash);
				}
				in.close();
				out.close();
				clientSocket.close();
//				System.out.println("recv");
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
