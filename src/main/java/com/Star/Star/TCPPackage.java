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
	private List<TransactionPackage> tp = null;
	private String hash;
	
	public TCPPackage(ServerAddress fromPeer, Block block) throws Exception {
		this.fromPeer = fromPeer;
		this.block = block;
		this.hash = block.getHash();
		this.isBlock = true;
	}
	
	public TCPPackage(ServerAddress fromPeer, List<TransactionPackage> tp) throws Exception {
		this.fromPeer = fromPeer;
		this.tp = tp;
		this.hash = tp.get(0).getHash();
		for(int i = 1; i < tp.size(); i++) {
			this.hash = new RSA().getSHA256(hash+tp.get(i));
		}
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
}
