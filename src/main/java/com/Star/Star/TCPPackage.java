package com.Star.Star;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Wrapper class to allow for sending both transactions and blocks
 */
public class TCPPackage implements Serializable {
	private final ServerAddress fromPeer;
	private final boolean isBlock;
	private Block block = null;
	private TransactionPackage tp = null;
	private String hash;
	
	public TCPPackage(ServerAddress fromPeer, Block block) throws Exception {
		this.fromPeer = fromPeer;
		this.block = block;
		this.hash = block.getHash();
		this.isBlock = true;
	}
	
	public TCPPackage(ServerAddress fromPeer, TransactionPackage tp) throws Exception {
		this.fromPeer = fromPeer;
		this.tp = tp;
		this.hash += tp.getHash();
		this.isBlock = false;
	}
	
	public Object getObject () {
		if (this.isBlock) {
			return this.block;
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
}
