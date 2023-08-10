package com.Star.Star;

import com.Star.Star.services.RSAService;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import static com.Star.Star.services.DateService.getCurrentTime;

public class Block implements Serializable{
	
	PrivateKey sk; //todo isaac decide whether or not to remove this, block does not sign itself we need a service class to sign a block
	BlockBody blockBody;
	String blockSig;

	public Block(PrivateKey sk, PublicKey pk, String prevBlockHash) {
		this.sk = sk;
		this.blockBody = new BlockBody(prevBlockHash, pk);
	}
	
	public void addTransaction(TransactionPackage transaction) throws Exception {
			this.blockBody.addTransaction(transaction);
	}

	public void signBlock() throws Exception {
		this.blockBody.timestamp = getCurrentTime();
		this.blockSig = RSAService.sign(this.blockBody.getHash(), sk);
	}
	
	public String getHash() throws NoSuchAlgorithmException { return this.blockBody.getHash(); }

	public BlockBody getBlockBody() { return blockBody; }

	public String getBlockSig() { return blockSig; }

	public List<TransactionPackage> getTransactions() {
		return this.blockBody.block;
	}
	
}
