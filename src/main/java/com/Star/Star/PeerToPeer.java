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

import com.Star.Star.services.RSAService;

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
	private boolean close = false;

	public PeerToPeer(String ip, int port, ServerAddress peer, int maxTpChunckSize) throws Exception {
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
			System.err.println("!!" + e.getMessage());
		}

		Thread sendLoop = new Thread(() -> {
            try {
                loopSend();
            } catch (Exception e) {
                System.err.println("??" + e.getMessage());
            }
        });
		sendLoop.start();
	}

	public void addToSend(Object msg) throws Exception {
		TCPPackage tcpPack = null;
		if (msg instanceof TransactionPackage) {
			TransactionPackage tp = (TransactionPackage) msg;
			tcpPack = new TCPPackage(new ServerAddress(this.ip, this.port), tp);
			if(this.toSend.containsKey(tcpPack.getHash()))
				System.out.println("Already Here!!!");
			this.toSend.put(tcpPack.getHash(), tcpPack);
		} else {
			Block block = (Block) msg;
			tcpPack = new TCPPackage(new ServerAddress(this.ip, this.port), block);
			if(this.toSend.containsKey(tcpPack.getHash()))
				System.out.println("Already Here!!!");
			this.toSend.put(tcpPack.getHash(), tcpPack);
		}
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
			Iterator<Entry<String, TCPPackage>> tcpIteratorBlocks = this.toSend.entrySet().iterator();
			while (tcpIteratorBlocks.hasNext()) {
				try {
					TCPPackage tcpPack = tcpIteratorBlocks.next().getValue();
					if (tcpPack.isBlock()) {
						out.writeObject(tcpPack);
						String hash = (String) in.readObject();
						this.toSend.remove(hash);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			while (tcpIterator.hasNext()) {
				try {
					TCPPackage tcpPack = tcpIterator.next().getValue();
					out.writeObject(tcpPack);
					String hash = (String) in.readObject();
					this.toSend.remove(hash);
					break;
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}
		try {
			out.close();
			in.close();
		} catch (Exception e) {
			System.err.println("{{" + e.getMessage());
		}
	}

	public void start() throws IOException {
		new Receive(this.serverSocket.accept()).start();
	}
	
	public void close() {
		close = true;
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
				System.out.println("Start recv ");
				while (!close) {
					Object objRecieved = in.readObject();
					tcpPack = (TCPPackage) objRecieved;
					String hash = tcpPack.getHash();
					out.writeObject(hash);
					Object msg = tcpPack.getObject();
					onRecieveMessage(msg);
				}
				clientSocket.close();
			} catch (Exception e) {
				// System.err.println("^^" + e.getMessage());
			}
		}
	}
}
