package com.Star.Star.BlockChainList.BlockChainListParts;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties.AssertingParty.Verification;

import com.Star.Star.BlockChainList.services.RSAService;
import com.Star.Star.BlockChainList.services.ValidationService;

/**
 * Peer to peer code for blockchain
 */
public abstract class PeerToPeer {
	private PublicKey pk;
	private PrivateKey sk;
	private String name;
	private String ip;
	private int port;
	private ServerAddress[] peers;
	private ServerSocket[] serverSockets;
	private Socket[] sendSockets;
	protected ConcurrentHashMap<String, TCPPackage> toSend = new ConcurrentHashMap<String, TCPPackage>();
	protected ConcurrentHashMap<String, Nounce> toAdd = new ConcurrentHashMap<String, Nounce>();
	protected ConcurrentHashMap<String, Boolean> verifiedBlocks = new ConcurrentHashMap<String, Boolean>();
	protected boolean close = false;
	ObjectOutputStream[] outs;
	ObjectInputStream[] ins;

	public PeerToPeer(PublicKey pk, PrivateKey sk, String name, String ip, int port, ServerAddress[] peers, int maxTpChunckSize) throws Exception {
		this.pk = pk;
		this.sk = sk;
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.peers = peers;
		if (peers != null) {
			this.sendSockets = new Socket[this.peers.length];
			this.serverSockets = new ServerSocket[this.peers.length];
			for (int i = 0; i < this.peers.length; i++) {
				this.serverSockets[i] = new ServerSocket(port+i);
			}
		}
		Thread recv = new Thread(new Runnable() {
			public void run() {
				try {
					start();
				} catch (IOException e) {}
			}
		});
		recv.start();

	}

	public void connectToPeer() throws IOException {
		try {
			for (int i = 0; i < peers.length; i++) {
				this.sendSockets[i] = new Socket(this.peers[i].getIp(), this.peers[i].getPort());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("!!" + e.getMessage());
		}

		
		
		this.outs = new ObjectOutputStream[peers.length];
		this.ins = new ObjectInputStream[peers.length];
		for (int i = 0; i < peers.length; i++) {
			this.outs[i] = new ObjectOutputStream(sendSockets[i].getOutputStream());
			this.ins[i] = new ObjectInputStream(sendSockets[i].getInputStream());
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
			TCPPackage tcpPack = new TCPTransactionPackagePackage(tp);
			this.toSend.put(tcpPack.getHash(), tcpPack);
		} else if (msg instanceof Block) {
			Block block = (Block) msg;
			TCPPackage tcpPack = new TCPBlockPackage(block);
			this.toSend.put(tcpPack.getHash(), tcpPack);
		} else if (msg instanceof BlockChainTCPPackage) {
			BlockChainTCPPackage bc = (BlockChainTCPPackage) msg;
			TCPPackage tcpPack = new TCPBlockChainPackage(bc);
			this.toSend.put(tcpPack.getHash(), tcpPack);
		}
	}

	public void loopSend() throws Exception {
		//loop sending while not closed
		while (!this.close) {
			Iterator<Entry<String, TCPPackage>> tcpIteratorToSend1 = this.toSend.entrySet().iterator();
			Iterator<Entry<String, TCPPackage>> tcpIteratorToSend2 = this.toSend.entrySet().iterator();
			while (tcpIteratorToSend1.hasNext() && !this.close) {
				TCPPackage tcpPack = tcpIteratorToSend1.next().getValue();
				if (tcpPack instanceof TCPBlockChainPackage || tcpPack instanceof TCPBlockPackage) {
					sendTCP(tcpPack);
				}
			}
			if (tcpIteratorToSend2.hasNext()) {
				TCPPackage tcpPack = tcpIteratorToSend2.next().getValue();
				sendTCP(tcpPack);
			}
		}
	}

	protected void sendTCP(TCPPackage tcpPack) throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
		ArrayList<String> signatureOfHash = new ArrayList<String>();
		ArrayList<PublicKey> pks = new ArrayList<PublicKey>();
		boolean verify = true;
		for (int i = 0; i < peers.length; i++) {
			this.outs[i].writeObject(tcpPack);
			TCPResponse res = (TCPResponse) this.ins[i].readObject();
			this.toSend.remove(res.getHash());
			signatureOfHash.add(res.getHashSignature());
			pks.add(res.getPublicKey());
			if (!res.getGuarantee()) {
				verify = false;
			}
		}
		if (tcpPack instanceof TCPTransactionPackagePackage) {
			if (verify) {
				this.toAdd.put(tcpPack.getHash(), new Nounce(signatureOfHash, pks, tcpPack.getHash()));
			} else {
				System.out.println(((TransactionPackage)tcpPack.getPackage()).toString());
			}
		}
		if (tcpPack instanceof TCPBlockPackage) {
			this.verifiedBlocks.put(tcpPack.getHash(), verify);
		}
	}


	public void start() throws IOException {
		if (peers != null) {
			for (int i = 0; i < this.peers.length; i++) {
				new Receive(this.serverSockets[i].accept()).start();
			}
		}
	}

	public void close() {
		close = true;
	}

	public abstract void onRecieveMessage(Object msg) throws Exception;

	public abstract String getPrevHash() throws Exception;

	public abstract List<TransactionPackage> getOnChainTransaction();

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
					Object msg = tcpPack.getPackage();
					if (msg instanceof TransactionPackage) {
						out.writeObject(new TCPResponse(pk, sk, hash, new ValidationService().validateTransactionMetadata((TransactionPackage) msg)));
					} else if (msg instanceof Block) {
						out.writeObject(new TCPResponse(pk, sk, hash, new ValidationService().validateBlock((Block) msg, getPrevHash(), getOnChainTransaction())));
					} else {
						out.writeObject(new TCPResponse(pk, sk, hash, true));
					}
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

}
