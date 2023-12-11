package com.Star.Star;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.Star.Star.services.RSAService;

/**
 * Wrapper class to allow for sending both transactions and blocks
 */
public class TCPPackage implements Serializable {
	private final ServerAddress fromPeer;
	private final boolean isBlock;
	private final boolean isTransactionPackage;
	private final boolean isBlockChain;
	private final Block block;
	private final BlockChainTCPPackage blockChain;
	private final TransactionPackage tp;
	private final String hash;
	
	public TCPPackage(ServerAddress fromPeer, Block block) throws Exception {
		this.fromPeer = fromPeer;
		this.block = block;
		this.blockChain = null;
		this.tp = null;
		this.hash = block.getHash();
		this.isBlock = true;
		this.isBlockChain = false;
		this.isTransactionPackage = false;
	}
	
	public TCPPackage(ServerAddress fromPeer, TransactionPackage tp) throws Exception {
		this.fromPeer = fromPeer;
		this.tp = tp;
		this.block = null;
		this.blockChain = null;
		this.hash = tp.getHash();
		this.isTransactionPackage = true;
		this.isBlock = false;
		this.isBlockChain = false;
	}
	
	public TCPPackage(ServerAddress fromPeer, BlockChainTCPPackage blockChain) throws Exception {
		this.fromPeer = fromPeer;
		this.blockChain = blockChain;
		this.tp = null;
		this.block = null;
		this.isBlockChain = true;
		this.isTransactionPackage = false;
		this.isBlock = false;
		this.hash = this.blockChain.getEntireHashOfBlockChain();
	}
	
	public Object getObject () throws Exception {
		if (this.isBlock) {
			return this.block;
		} else if (this.isBlockChain) {
			return this.blockChain;
		} else {
			return this.tp;
		}
	}
	
	public String getHash() {
		return this.hash;
	}
	
	public boolean isBlock() {
		return this.isBlock;
	}
	
	public boolean isBlockChain() {
		return this.isBlockChain;
	}

	public boolean isTransactionPackage() {
		return this.isTransactionPackage;
	}

}
