package com.Star.Star.BlockChainList.BlockChainListParts;

import java.io.Serializable;

/**
 * Wrapper class to allow for sending both transactions and blocks
 */
public class TCPTransactionPackagePackage extends TCPPackage implements Serializable {
	private final TransactionPackage tp;
	private final String hash;
	
	
	public TCPTransactionPackagePackage(TransactionPackage tp) throws Exception {
		this.tp = tp;
		this.hash = tp.getHash();
	}

	public TransactionPackage getPackage () {
		return this.tp;
	}
	
	public String getHash() {
		return this.hash;
	}

}
