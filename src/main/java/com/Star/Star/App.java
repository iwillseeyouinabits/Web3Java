package com.Star.Star;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.UnknownHostException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 *
 */
public class App {
	
    public static void main( String[] args ) throws Exception {
    	testBatchRun(1000000);
//    	testPeerToPeer(20, 6942);
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
    
    public static void testBatchRun(int numToRun) throws Exception {
    	Thread[] runners = new Thread[numToRun];
    	KeyPair kp = new RSA().generateKeyPair();
    	List<Entry<String, Integer>> peers = new ArrayList<Entry<String, Integer>>();
    	List blockChainList = Collections.synchronizedList(new BlockChainList(kp.getPrivate(), kp.getPublic(), 4, "localhost", 42069, peers));
    	List<TransactionPackage> tps = Collections.synchronizedList(new ArrayList<TransactionPackage>());
    	ExecutorService executor = Executors.newFixedThreadPool(30);    		
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

    	for (int i = 0; i < numToRun; i++) {
    		TransactionBody tb;
    		if (Math.random() > 2.0 / 3.0) {
    			tb = new HttpTransaction(keys1.getPublic(), keys2.getPublic(), keys3.getPublic(), "{'Hello': 'World!'}");
    		} else if (Math.random() > 1.0 / 2.0) {
    			tb = new CurrencyTransaction(keys1.getPublic(), keys2.getPublic(), 5);
    		} else {
    			tb = new ShellTransaction(keys1.getPublic(), "echo 'HELLO WORLD'", "Ride Share");
    		}
			TransactionPackage tp = new TransactionPackage(tb, keys1.getPrivate());
			tps.add(tp);
			if (i%10000==0) {
				System.out.println((double)(numToRun-i)/numToRun);
			}
    	}
    	
    	BlockTesterThreaded btt = new BlockTesterThreaded(blockChainList, tps);
    	
    	long launchTime = new Date().getTime();
    	for (int i = 0; i < runners.length; i++) {
    		runners[i] = new Thread(btt);
    		executor.submit(runners[i]);
    	}
    	double joinTime = new Date().getTime();
    	
    	
    	while(blockChainList.size() != numToRun) {}
    	executor.shutdown();
    	System.out.println("JOINED!");
    	double finishTime = new Date().getTime();
    	
    	
    	int numVerified = numToRun;
    	for (int i = 1; i < blockChainList.size(); i*=2) {
    		if (!((TransactionPackage) blockChainList.get(i)).verifySigner()) {
    			numVerified--;
    		}
    	}

    	System.out.println("numAdded: " + blockChainList.size() + " V.S. " + numToRun);
    	System.out.println("numVerifiedSig: " + blockChainList.size() + " V.S. " + numVerified);
    	System.out.println("Launch in: " + (joinTime-launchTime)/1000.0);
    	System.out.println("Run in: " + (finishTime-joinTime)/1000.0);
    	System.out.println("TPS: "+(numToRun/((finishTime-joinTime)/1000.0)));
    }
}
