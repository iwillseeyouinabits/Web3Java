package com.Star.Star;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockTesterThreaded implements Runnable {
	List blockChain;
	List<TransactionPackage> tps;
	
	public BlockTesterThreaded(List blockChain, List<TransactionPackage> tps) {
		this.blockChain = blockChain;
		this.tps = tps;
	}

	public void run() {
		TransactionPackage tp = tps.get((int)(tps.size()*Math.random()));
		this.blockChain.add(tp);
	}
}
