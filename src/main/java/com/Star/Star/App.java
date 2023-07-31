package com.Star.Star;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 *
 */
public class App {

	public static void main(String[] args) throws Exception {
		int numToRun = 1000;
		int chunckSize = 5;
		testBatchRun(numToRun, chunckSize, 3, 100);
	}

//    public static void testPeerToPeer(int numberOfPeers, int startIP) throws UnknownHostException, IOException, InterruptedException {
//    	PeerToPeer[] p2ps = new PeerToPeer[numberOfPeers];
//    	Thread[] threads = new Thread[numberOfPeers];
//    	for (int i = 0; i < numberOfPeers; i++) {
//    		System.out.println(i);
//    		p2ps[i] = new PeerToPeer("127.0.0.1", startIP+i);
//    		Thread t = new Thread(p2ps[i]);
//    		t.start();
//    		threads[i] = t;
//    	}
//
//    	for (int i = 1; i < numberOfPeers; i++) {
//        	p2ps[i].sendMessage("Hello World! " + i, "127.0.0.1", startIP+i-1);
//    	}
//    	for (int i = 1; i < numberOfPeers; i++) {
//        	p2ps[i].sendMessage("Hello World! " + i, "127.0.0.1", startIP+i-1);
//    	}
//    	for (int i = 1; i < numberOfPeers; i++) {
//        	p2ps[i].sendMessage("Hello World! " + i, "127.0.0.1", startIP+i-1);
//    	}
//    	for (int i = 1; i < numberOfPeers; i++) {
//        	p2ps[i].sendMessage("Hello World! " + i, "127.0.0.1", startIP+i-1);
//    	}
//    	Thread.sleep(2000);
//    	for (int i = 1; i < numberOfPeers; i++) {
//        	p2ps[i].close();
//    	}
//    	for (int i = 0; i < numberOfPeers; i++) {
//        	threads[i].join();
//        	System.out.println(i);
//    	}
//    }

	public static void testBatchRun(int numToRun, int chunckSize, int numChains, int numPorts) throws Exception {
		Thread[] runners = new Thread[numToRun];
		KeyPair kp = new RSA().generateKeyPair();
		List<ServerAddress> peers = new ArrayList<ServerAddress>();
		for (int i = 0; i < numChains; i++) {
			peers.add(new ServerAddress("127.0.0.1", 4300 + (i*numPorts)));
		}
		BlockChainList[] blockChainListsThread = new BlockChainList[numChains];

		for (int i = 0; i < numChains; i++) {
			blockChainListsThread[i] = (new BlockChainList(kp.getPrivate(), kp.getPublic(), 4, "127.0.0.1", 4300 + (i*numPorts),
					peers, chunckSize, numPorts));
		}

		List[] blockChainLists = new List[numChains];
		for (int i = 0; i < numChains; i++) {
			blockChainLists[i] = Collections.synchronizedList(blockChainListsThread[i]);
		}

		List<TransactionPackage> tps = Collections.synchronizedList(new ArrayList<TransactionPackage>());
		ExecutorService executor = Executors.newFixedThreadPool(30);

		// gen public keys
		KeyPair keys1 = null;
		KeyPair keys2 = null;
		KeyPair keys3 = null;
		try {
			keys1 = new RSA().generateKeyPair();
			keys2 = new RSA().generateKeyPair();
			keys3 = new RSA().generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		// gen transaction
		for (int i = 0; i < numToRun; i++) {
			TransactionBody tb;
			if (Math.random() > 2.0 / 3.0) {
				tb = new HttpTransaction(keys1.getPublic(), keys2.getPublic(), keys3.getPublic(),
						"{'Hello': 'World!'}");
			} else if (Math.random() > 1.0 / 2.0) {
				tb = new CurrencyTransaction(keys1.getPublic(), keys2.getPublic(), 5);
			} else {
				tb = new ShellTransaction(keys1.getPublic(), "echo 'HELLO WORLD'", "Ride Share");
			}
			TransactionPackage tp = new TransactionPackage(tb, keys1.getPrivate());
			tps.add(tp);
			if (i % (numToRun / 10) == 0) {
				System.out.println((double) (numToRun - i) / numToRun);
			}
		}

		// execute threaded transactions
		BlockTesterThreaded btt = new BlockTesterThreaded(blockChainLists, tps);
		long launchTime = new Date().getTime();
		for (int i = 0; i < runners.length; i++) {
			runners[i] = new Thread(btt);
			executor.submit(runners[i]);
		}
		double joinTime = new Date().getTime();
		// join threads
		System.out.println("Bingo");
		while (blockChainLists[0].size() != numToRun) {
		}
		executor.shutdown();
		System.out.println("JOINED!");
		double finishTime = new Date().getTime();

		for (int i = 0; i < blockChainListsThread.length; i++) {
			blockChainListsThread[i].close();
		}
		for (int i = 0; i < runners.length; i++) {
			runners[i].join();
		}
		// verify transactions
		int numVerified = numToRun;
		for (int i = 1; i < blockChainLists[0].size(); i *= 2) {
			if (!((TransactionPackage) blockChainLists[0].get(i)).verifySigner()) {
				numVerified--;
			}
		}

		// print results
		System.out.println("numAdded: " + blockChainLists[0].size() + " V.S. " + numToRun);
		System.out.println("numVerifiedSig: " + blockChainLists[0].size() + " V.S. " + numVerified);
		System.out.println("Launch in: " + (joinTime - launchTime) / 1000.0);
		System.out.println("Run in: " + (finishTime - joinTime) / 1000.0);
		System.out.println("TPS: " + (numToRun / ((finishTime - joinTime) / 1000.0)));
	}
}
