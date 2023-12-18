package com.Star.Star;

import java.io.Serializable;

/**
 * Wrapper class to allow for sending both transactions and blocks
 */
public class TCPBlockChainPackage extends TCPPackage implements Serializable {
	private final BlockChainTCPPackage blockChain;
	private final String hash;

	public TCPBlockChainPackage(ServerAddress fromPeer, BlockChainTCPPackage blockChain) throws Exception {
		super();
		this.blockChain = blockChain;
		this.hash = this.blockChain.getEntireHashOfBlockChain();
	}
	
	public Object getPackage() {
		return this.blockChain;
	}
	
	public String getHash() {
		return this.hash;
	}

}
