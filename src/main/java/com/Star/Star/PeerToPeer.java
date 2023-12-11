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

	public void loopSend() {

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
			Iterator<Entry<String, TCPPackage>> tcpIteratorBlockChains = this.toSend.entrySet().iterator();
			while (tcpIteratorBlockChains.hasNext()) {
				try {
					TCPPackage tcpPack = tcpIteratorBlockChains.next().getValue();
					if (tcpPack.isBlockChain()) {
						// System.out.println("BlockChain Sending to Peer: " + ((BlockChainTCPPackage) tcpPack.getObject()).getEntireHashOfBlockChain());
						out.writeObject(tcpPack);
						// System.out.println("BlockChain Sent to Peer: " + ((BlockChainTCPPackage) tcpPack.getObject()).getEntireHashOfBlockChain());
						String hash = (String) in.readObject();
						// System.out.println("Hash Recieved From Peer: " + hash);
						this.toSend.remove(hash);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			while (tcpIteratorBlocks.hasNext()) {
				try {
					TCPPackage tcpPack = tcpIteratorBlocks.next().getValue();
					if (tcpPack.isBlock()) {
						out.writeObject(tcpPack);
						String hash = (String) in.readObject();
						this.toSend.remove(hash);
						break;
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
				} catch (Exception e) {
					e.printStackTrace();
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
					if (msg instanceof BlockChainTCPPackage) {
						// System.out.println("Recieved BlockChain " + ((BlockChainTCPPackage) msg).getEntireHashOfBlockChain()
						// 		+ " " + hash);
					}
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
