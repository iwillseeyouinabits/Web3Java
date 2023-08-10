package com.Star.Star;

import com.Star.Star.services.RSAService;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.Star.Star.services.DateService.getCurrentTime;

public class BlockBody implements Serializable {
	final String prevBlockHash;
	final PublicKey minerPk;
	long timestamp;
	List<TransactionPackage> block;
	String hashableToken;

	public String getPrevBlockHash() {return prevBlockHash;}

	public long getTimestamp() {return timestamp;}

	public PublicKey getMinerPk() {return minerPk;}

	public BlockBody(String prevBlockHash, PublicKey minerPk) {
		this.prevBlockHash = prevBlockHash;
		this.minerPk = minerPk;
		this.timestamp = getCurrentTime();
		this.block = Collections.synchronizedList(new ArrayList<>());
		hashableToken = this.prevBlockHash + RSAService.pkToString(this.minerPk);
	}

	public void addTransaction(TransactionPackage transaction) throws NoSuchAlgorithmException {
		block.add(transaction);
		this.hashableToken = RSAService.getSHA256(this.hashableToken+transaction.getHash());
	}

	public String getHash() throws NoSuchAlgorithmException { 
		return RSAService.getSHA256(hashableToken);
	}
	
	public String getHashableToken() throws NoSuchAlgorithmException {
		String hashableToken = this.prevBlockHash + RSAService.getSHA256(RSAService.pkToString(this.minerPk));
		for (TransactionPackage tp : this.block) {
			hashableToken += tp.getHash();
		}
		return hashableToken;
	}

}
