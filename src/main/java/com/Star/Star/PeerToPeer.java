package com.Star.Star;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.Star.Star.services.RSAService;

/**
 * Peer to peer code for blockchain
 */
public abstract class PeerToPeer {
	private String name;
	private String ip;
	private int port;
	private ServerAddress[] peers;
	private ServerSocket[] serverSockets;
	private Socket[] sendSockets;
	protected ConcurrentHashMap<String, TCPPackage> toSend = new ConcurrentHashMap<String, TCPPackage>();
	private boolean close = false;

	public PeerToPeer(String name, String ip, int port, ServerAddress[] peers, int maxTpChunckSize) throws Exception {
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.peers = peers;
		this.sendSockets = new Socket[this.peers.length];
		this.serverSockets = new ServerSocket[this.peers.length];
		for (int i = 0; i < this.peers.length; i++) {
			System.out.println(name +  " - " + (port + i));
			this.serverSockets[i] = new ServerSocket(port+i);
		}

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
			for (int i = 0; i < peers.length; i++) {
				System.out.println(this.name + " " + this.peers[i].getPort());
				this.sendSockets[i] = new Socket(this.peers[i].getIp(), this.peers[i].getPort());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("!!" + e.getMessage());
		}

		Thread sendLoop = new Thread(() -> {
			try {
				loopSend();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("??" + e.getMessage());
			}
		});
		sendLoop.start();
	}

	public void addToSend(Object msg) throws Exception {
		if (msg instanceof TransactionPackage) {
			TransactionPackage tp = (TransactionPackage) msg;
			TCPPackage tcpPack = new TCPPackage(new ServerAddress(this.ip, this.port), tp);
			this.toSend.put(tcpPack.getHash(), tcpPack);
		} else if (msg instanceof Block) {
			Block block = (Block) msg;
			TCPPackage tcpPack = new TCPPackage(new ServerAddress(this.ip, this.port), block);
			this.toSend.put(tcpPack.getHash(), tcpPack);
		} else if (msg instanceof BlockChainTCPPackage) {
			BlockChainTCPPackage bc = (BlockChainTCPPackage) msg;
			TCPPackage tcpPack = new TCPPackage(new ServerAddress(this.ip, this.port), bc);
			this.toSend.put(tcpPack.getHash(), tcpPack);
			// System.out.println("Added BlockChain To Send Queue "
			// 		+ tcpPack.getHash());
		}
	}

	public void loopSend() throws IOException {

		ObjectOutputStream[] outs = new ObjectOutputStream[peers.length];
		ObjectInputStream[] ins = new ObjectInputStream[peers.length];

		for (int i = 0; i < peers.length; i++) {
			outs[i] = new ObjectOutputStream(sendSockets[i].getOutputStream());
			ins[i] = new ObjectInputStream(sendSockets[i].getInputStream());
		}
		

		while (!this.close) {
			Iterator<Entry<String, TCPPackage>> tcpIterator = this.toSend.entrySet().iterator();
			Iterator<Entry<String, TCPPackage>> tcpIteratorBlocks = this.toSend.entrySet().iterator();
			Iterator<Entry<String, TCPPackage>> tcpIteratorBlockChains = this.toSend.entrySet().iterator();
			while (tcpIteratorBlockChains.hasNext()) {
				try {
					TCPPackage tcpPack = tcpIteratorBlockChains.next().getValue();
					if (tcpPack.isBlockChain()) {
						for (int i = 0; i < peers.length; i++) {
							outs[i].writeObject(tcpPack);
							String hash = (String) ins[i].readObject();
							this.toSend.remove(hash);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			while (tcpIteratorBlocks.hasNext()) {
				try {
					TCPPackage tcpPack = tcpIteratorBlocks.next().getValue();
					if (tcpPack.isBlock()) {
						for (int i = 0; i < peers.length; i++) {
							outs[i].writeObject(tcpPack);
							String hash = (String) ins[i].readObject();
							this.toSend.remove(hash);
						}
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			while (tcpIterator.hasNext()) {
				try {
					TCPPackage tcpPack = tcpIterator.next().getValue();
					for (int i = 0; i < peers.length; i++) {
						outs[i].writeObject(tcpPack);
						String hash = (String) ins[i].readObject();
						this.toSend.remove(hash);
					}
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		try {
			for (int i = 0; i < peers.length; i++) {
				outs[i].close();
				ins[i].close();
			}
		} catch (Exception e) {
			System.err.println("{{" + e.getMessage());
		}
	}

	public void start() throws IOException {
		for (int i = 0; i < this.peers.length; i++) {
			new Receive(this.serverSockets[i].accept()).start();
		}
	}

	public void close() {
		close = true;
	}

	public abstract void onRecieveMessage(Object msg) throws Exception;

	public class Receive extends Thread {
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
					final TCPPackage tcpPack = (TCPPackage) in.readObject();
					String hash = tcpPack.getHash();
					Object msg = tcpPack.getObject();
					out.writeObject(hash);
					onRecieveMessage(msg);
				}
				clientSocket.close();
			} catch(EOFException e) {
				System.err.println("#--> Finish");
			} catch (Exception e) {
				System.err.println(";-->" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public String getEntireHashOfBlockChain(Map<String, Block> bc) throws NoSuchAlgorithmException {
		String prevHash = "000000000000000";
		String hashes = prevHash;
		while (bc.containsKey(prevHash)) {
			Block curBlock = bc.get(prevHash);
			prevHash = curBlock.getHash();
			hashes += prevHash;
		}
		return new RSAService().getSHA256(hashes);
	}

}
