package com.Star.Star.BlockChainList.BlockChainListParts;

import java.io.Serializable;

/**
 * Wrapper class to allow for sending both transactions and blocks
 */
public class TCPBlockPackage extends TCPPackage implements Serializable {
	private final Block block;
	private final String hash;
	
	public TCPBlockPackage(Block block) throws Exception {
		super();
		this.block = block;
		this.hash = block.getHash();
	}
	
	public Block getPackage() {
		return this.block;
	}
	
	public String getHash() {
		return this.hash;
	}

}
