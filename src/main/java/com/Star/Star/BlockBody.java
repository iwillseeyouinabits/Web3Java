package com.Star.Star;

import lombok.Data;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Data
public class BlockBody implements Serializable {
	final String prevBlockHash;
	final PublicKey minerPk;
	long timestamp;
	List<TransactionPackage> block;
	String hashableToken;

	public BlockBody(String prevBlockHash, PublicKey minerPk) throws NoSuchAlgorithmException {
		this.prevBlockHash = prevBlockHash;
		this.minerPk = minerPk;
		this.timestamp = new Date().getTime() / 1000 / 60;
		this.block = Collections.synchronizedList(new ArrayList<TransactionPackage>());
		hashableToken = this.prevBlockHash + new RSA().pkToString(this.minerPk);
	}

	public void addTransaction(TransactionPackage transaction) throws NoSuchAlgorithmException {
		block.add(transaction);
		this.hashableToken = new RSA().getSHA256(this.hashableToken+transaction.getHash());
	}

	public String getHash() throws NoSuchAlgorithmException { 
		return new RSA().getSHA256(hashableToken);
	}
	
	public String getHashableToken() throws NoSuchAlgorithmException {
		String hashableToken = this.prevBlockHash + new RSA().getSHA256(new RSA().pkToString(this.minerPk));
		for (TransactionPackage tp : this.block) {
			hashableToken += tp.getHash();
		}
		return hashableToken;
	}

}
