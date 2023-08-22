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
	private List<TransactionPackage> tps = null;
	private String hash;
	
	public TCPPackage(ServerAddress fromPeer, Block block) throws Exception {
		this.fromPeer = fromPeer;
		this.block = block;
		this.hash = block.getHash();
		this.isBlock = true;
	}
	
	public TCPPackage(ServerAddress fromPeer, List<TransactionPackage> tps) throws Exception {
		this.fromPeer = fromPeer;
		this.tps = tps;
		this.hash = "";
		for (TransactionPackage tp : tps)
			this.hash += tp.getHash();
		this.isBlock = false;
	}
	
	public Object getObject () {
		if (this.isBlock) {
			return this.block;
		} else {
			return this.tps;
		}
	}
	
	public String getHash() {
		return this.hash;
	}
	
	public ServerAddress getPeer() {
		return this.fromPeer;
	}
}
