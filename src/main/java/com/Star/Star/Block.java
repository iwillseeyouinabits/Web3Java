package com.Star.Star;

import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Block {
	
	PrivateKey sk;
	BlockBody blockBody;
	String blockSig;
	
	public Block(PrivateKey sk, PublicKey pk, String prevBlockHash) throws NoSuchAlgorithmException {
		this.sk = sk;
		this.blockBody = new BlockBody(prevBlockHash, pk);
	}
	
	public void addTransaction(TransactionPackage transaction) throws Exception {
			this.blockBody.addTransaction(transaction);
	}
	
	public void signBlock() throws NoSuchAlgorithmException, Exception {
		this.blockBody.timestamp = new Date().getTime() / 1000 / 60;
		this.blockSig = new RSA().sign(this.blockBody.getHash(), sk);
	}
	
	public String getHash() throws Exception {
		return this.blockBody.getHash();
	}
	
	public List<TransactionPackage> getTransactions() {
		return this.blockBody.block;
	}
	
}
