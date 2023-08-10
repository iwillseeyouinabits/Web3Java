package com.Star.Star;

import java.util.Collections;
import java.util.List;

/**
 * Process to test single threads acceptance of a transaction
 */
public class BlockTesterThreaded implements Runnable {
	List[] blockChain;
	List<TransactionPackage> transactionPackages;
	
	public BlockTesterThreaded(List[] blockChain, List<TransactionPackage> transactionPackages) {
		this.blockChain = blockChain;
		this.transactionPackages = Collections.synchronizedList(transactionPackages);
	}

	public void run() {
		int index = this.transactionPackages.size();
		TransactionPackage tp = transactionPackages.remove(0);
		this.blockChain[index%blockChain.length].add(tp);
	}
}
