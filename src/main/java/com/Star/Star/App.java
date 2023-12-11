package com.Star.Star;

import com.Star.Star.services.RSAService;
import com.Star.Star.services.ValidationService;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {

	public static void main(String[] args) throws Exception {
		int numToRun = 1000;
		int numChains = 3;
		int maxDifficulty = 4;
		testBatchRun(numToRun, numChains, maxDifficulty);
	}

	public static void testBatchRun(int numToRun, int numChains, int maxDifficulty) throws Exception {
		Thread[] runners = new Thread[numToRun];
		KeyPair[] kps = new KeyPair[numChains];
		for (int i = 0; i < numChains; i++) {
			kps[i] = new RSAService().generateKeyPair();
		}

		// Initiating synchronised blockchains for each peer
		BlockChainList[] unsyncedBlockChainLists = new BlockChainList[numChains];
		int[] peersItr = new int[numChains];
		for (int i = 0; i < numChains; i++) {

			ServerAddress[] peers = null;
			if (numChains > 1) {
				int peerItr = 0;
				peers = new ServerAddress[numChains-1];
				for (int j = 0; j < numChains; j++) {
					if (j != i) {
						peers[peerItr] = new ServerAddress("127.0.0.1", 42069 + ((numChains-1)*j) + peersItr[j]);
						peersItr[j]++;
						peerItr++;
					}
				}
			}

			unsyncedBlockChainLists[i] = new BlockChainList("Miner" + i, kps[i].getPrivate(), kps[i].getPublic(), maxDifficulty,
					"127.0.0.1", 42069 + (i*(numChains-1)),
					peers, maxDifficulty);
		}

		if (numChains > 1)
			for (int i = 0; i < numChains; i++) {
				unsyncedBlockChainLists[i].connectToPeer();
			}

		List[] syncedBlockChainLists = new List[numChains];
		for (int i = 0; i < numChains; i++) {
			syncedBlockChainLists[i] = Collections.synchronizedList(unsyncedBlockChainLists[i]);
		}

		List<TransactionPackage> syncedTransactionPackages = Collections
				.synchronizedList(new ArrayList<TransactionPackage>());
		ExecutorService executor = Executors.newFixedThreadPool(30);

		// Initiating keys
		KeyPair keys1 = null;
		KeyPair keys2 = null;
		KeyPair keys3 = null;
		try {
			keys1 = RSAService.generateKeyPair();
			keys2 = RSAService.generateKeyPair();
			keys3 = RSAService.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		// generating test transactions
		ProgressBar dataGenProgressBar = new ProgressBar(
				"Genearate Moch Data To Load Test Network. Moch Data Left to Gen ->", numToRun, ProgressBarStyle.ASCII);
		while (dataGenProgressBar.getCurrent() != numToRun) {
			Transaction genericTransaction;
			if (Math.random() > 2.0 / 3.0) {
				genericTransaction = new HttpTransaction(keys1.getPublic(), keys2.getPublic(), keys3.getPublic(),
						"{'Hello': 'World!'}", UUID.randomUUID().toString());
			} else if (Math.random() > 1.0 / 2.0) {
				genericTransaction = new CurrencyTransaction(keys1.getPublic(), keys2.getPublic(), 5,
						UUID.randomUUID().toString());
			} else {
				genericTransaction = new ShellTransaction(
						keys1.getPublic(),
						"while true; do { \\\n  echo -ne \"HTTP/1.0 200 OK\\r\\nContent-Length: $(wc -c <\"<HTML><BODY><H1>Hello World!</H1></BODY></HTML>\")\\r\\n\\r\\n\"; \\\n  cat \"<HTML><BODY><H1>Hello World!</H1></BODY></HTML>\"; } | nc -l -p 8080 ; \\ \ndone",
						"Ride Share", UUID.randomUUID().toString());
			}
			TransactionPackage tp = new TransactionPackage(genericTransaction, keys1.getPrivate());
			syncedTransactionPackages.add(tp);
			dataGenProgressBar.step();
		}
		dataGenProgressBar.close();
		System.out.println();
		// testing tps for processing transactions
		BlockTesterThreaded btt = new BlockTesterThreaded(syncedBlockChainLists, syncedTransactionPackages);
		long launchTime = new Date().getTime();
		ProgressBar launchingMochDataToNetworkProgressBar = new ProgressBar(
				"Launching Moch Data To Network. Moch Data Left to Launch -> ", numToRun, ProgressBarStyle.ASCII);
		while (launchingMochDataToNetworkProgressBar.getCurrent() != numToRun) {
			int i = (int) launchingMochDataToNetworkProgressBar.getCurrent();
			runners[i] = new Thread(btt);
			executor.submit(runners[i]);
			launchingMochDataToNetworkProgressBar.step();
		}
		launchingMochDataToNetworkProgressBar.close();
		System.out.println();
		double joinTime = new Date().getTime();

		// join threads
		ProgressBar dataProcessedProgressBar = new ProgressBar(
				"Data Processed By Network. Data Left to be Processed -> ", numToRun, ProgressBarStyle.ASCII);
		while (dataProcessedProgressBar.getCurrent() < numToRun) {
			dataProcessedProgressBar.stepTo(unsyncedBlockChainLists[0].size());
		}
		dataProcessedProgressBar.close();
		System.out.println();
		int numProcessed = syncedBlockChainLists[0].size();
		double finishTime = new Date().getTime();
		executor.shutdown();
		System.out.println();
		System.out.println();

		for (int i = 0; i < runners.length; i++) {
			runners[i].join();
		}

		Thread.sleep(5000);
		// print results
		System.out.println("numAdded: " + syncedBlockChainLists[0].size() + " V.S. " + numToRun);
		System.out.println("Launch in: " + (joinTime - launchTime) / 1000.0);
		System.out.println("Run in: " + (finishTime - joinTime) / 1000.0);
		System.out.println("TPS: " + (numProcessed / ((finishTime - joinTime) / 1000.0)));

		// if (syncedBlockChainLists.length > 1) {
		// for (int i = 0; i < unsyncedBlockChainLists[0].getBlockChainList().size()
		// && i < unsyncedBlockChainLists[1].getBlockChainList().size(); i++) {
		// System.out.println(unsyncedBlockChainLists[0].getBlockChainList().get(i).getHash()
		// + " <=> "
		// + unsyncedBlockChainLists[1].getBlockChainList().get(i).getHash());
		// for (int j = 0; j <
		// unsyncedBlockChainLists[0].getBlockChainList().get(i).blockBody.block.size()
		// && j < 10; j++) {
		// System.out.println(" -> "
		// +
		// unsyncedBlockChainLists[0].getBlockChainList().get(i).blockBody.block.get(j).getHash()
		// + " <-> "
		// +
		// unsyncedBlockChainLists[1].getBlockChainList().get(i).blockBody.block.get(j).getHash());
		// }
		// }
		// }
		for (int i = 0; i < syncedBlockChainLists.length; i++) {
			unsyncedBlockChainLists[i].writeToFile("BlockChain" + i + ".json");
			unsyncedBlockChainLists[i].close();
		}
	}
}
