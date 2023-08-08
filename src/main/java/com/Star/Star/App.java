package com.Star.Star;

import com.Star.Star.services.ValidationService;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {

	public static void main(String[] args) throws Exception {
		int numToRun = 100000;
		int numChains = 2;
		testBatchRun(numToRun, numChains);
	}

	public static void testBatchRun(int numToRun, int numChains) throws Exception {
		Thread[] runners = new Thread[numToRun];
		KeyPair kp = new RSA().generateKeyPair();
		ServerAddress[] peers = new ServerAddress[numChains];
		if (numChains > 1) {
			for (int i = 0; i < numChains; i++) {
				peers[i] = new ServerAddress("127.0.0.1", 42069 + ((i + 1) % numChains));
			}
		}

		/**
		 * Initiating synchronised blockchains for each peer
		 */
		BlockChainList[] unsyncedBlockChainLists = new BlockChainList[numChains];

		for (int i = 0; i < numChains; i++) {
			unsyncedBlockChainLists[i] = new BlockChainList(kp.getPrivate(), kp.getPublic(), 4, "127.0.0.1", 42069 + i,
					peers[i]);
		}

		if (numChains > 1)
			for (int i = 0; i < numChains; i++) {

				unsyncedBlockChainLists[i].connectToPeer();
			}

		BlockChainList[] syncedBlockChainLists = new BlockChainList[numChains];
		for (int i = 0; i < numChains; i++) {
			syncedBlockChainLists[i] = (BlockChainList) Collections.synchronizedList(unsyncedBlockChainLists[i]);
		}

		List<TransactionPackage> syncedTransactionPackages = Collections.synchronizedList(new ArrayList<TransactionPackage>());
		ExecutorService executor = Executors.newFixedThreadPool(30);

		/**
		 * Initiating keys
		 */
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
		/**
		 * generating test transactions
		 */
		for (int i = 0; i < numToRun; i++) {
			Transaction genericTransaction;
			if (Math.random() > 2.0 / 3.0) {
				genericTransaction = new HttpTransaction(keys1.getPublic(), keys2.getPublic(), keys3.getPublic(),
						"{'Hello': 'World!'}");
			} else if (Math.random() > 1.0 / 2.0) {
				genericTransaction = new CurrencyTransaction(keys1.getPublic(), keys2.getPublic(), 5);
			} else {
				genericTransaction = new ShellTransaction(
						keys1.getPublic(), "echo 'HELLO WORLD'", "Ride Share");
			}
			TransactionPackage tp = new TransactionPackage(genericTransaction, keys1.getPrivate());
			syncedTransactionPackages.add(tp);
			if (i % (numToRun / 10) == 0) {
				System.out.println((double) (numToRun - i) / numToRun);
			}
		}

		/**
		 * testing tps for processing transactions
		 */
		BlockTesterThreaded btt = new BlockTesterThreaded(syncedBlockChainLists, syncedTransactionPackages);
		long launchTime = new Date().getTime();
		System.out.println("Initiating Thread Tests");
		for (int i = 0; i < runners.length; i++) {
			runners[i] = new Thread(btt);
			executor.submit(runners[i]);
		}
		double joinTime = new Date().getTime();
		// join threads
		System.out.println("Bingo");
		while (syncedBlockChainLists[0].size() < numToRun) {
			Thread.sleep(1000);
			System.out.println(">> " + unsyncedBlockChainLists[0].size());
		}
		int numProcessed = syncedBlockChainLists[0].size();
		double finishTime = new Date().getTime();
		executor.shutdown();
		System.out.println("JOINED!");

		for (int i = 0; i < runners.length; i++) {
			runners[i].join();
		}
		/**
		 * "randomly" verify block signatures
		 */
		final ValidationService validationService = new ValidationService();
		int numVerified = numToRun;
		for (int i = 1; i < syncedBlockChainLists[0].size(); i *= 2) {
			if (!((TransactionPackage) syncedBlockChainLists[0].get(i)).verifySigner()) {
				numVerified--;
			}
		}

		// print results
		System.out.println("numAdded: " + syncedBlockChainLists[0].size() + " V.S. " + numToRun);
		System.out.println("numVerifiedSig: " + syncedBlockChainLists[0].size() + " V.S. " + numVerified);
		System.out.println("Launch in: " + (joinTime - launchTime) / 1000.0);
		System.out.println("Run in: " + (finishTime - joinTime) / 1000.0);
		System.out.println("TPS: " + (numProcessed / ((finishTime - joinTime) / 1000.0)));
	}
}
