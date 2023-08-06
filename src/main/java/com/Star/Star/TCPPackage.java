package com.Star.Star;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TCPPackage implements Serializable {
	private ServerAddress fromPeer;
	private boolean isBlock;
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
		this.hash = tp.getHash();
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
	
	public ServerAddress getPeer() {
		return this.fromPeer;
	}
}
