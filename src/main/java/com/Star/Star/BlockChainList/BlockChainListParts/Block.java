package com.Star.Star.BlockChainList.BlockChainListParts;

import static com.Star.Star.BlockChainList.services.DateService.getCurrentTime;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Star.Star.BlockChainList.services.RSAService;

public class Block implements Serializable{
	
	BlockBody blockBody;
	String blockSig;

	public Block(PublicKey pk, String prevBlockHash) throws NoSuchAlgorithmException {
		this.blockBody = new BlockBody(prevBlockHash, pk);
	}
	
	public void addTransaction(TransactionPackage transaction, Nounce nounce) throws Exception {
			this.setNounce(nounce);
			this.blockBody.addTransaction(transaction);
	}
	
	public void addTransaction(TransactionPackage transaction) throws Exception {
			this.blockBody.addTransaction(transaction);
	}


	public void signBlock(PrivateKey sk) throws Exception {
		this.blockBody.timestamp = getCurrentTime();
		this.blockSig = RSAService.sign(this.blockBody.getHash(), sk);
	}

	public void setNounce(Nounce n) {
		this.blockBody.setNounce(n);
	}
	
	public String getHash() throws Exception { 
		return this.blockBody.getHash(); 
	}

	public BlockBody getBlockBody() { return blockBody; }

	public String getBlockSig() { return blockSig; }

	public List<TransactionPackage> getTransactions() {
		return this.blockBody.block;
	}

	public JSONObject getJson() throws Exception {
		JSONObject json = new JSONObject();
		json.put("Hash", this.getHash());
		json.put("Miner Signature", this.blockSig);
		if(this.blockBody.getNounce() != null)
			json.put("Nounce", this.blockBody.getNounce().getJson());
		json.put("Body", blockBody.getJson());
		return json;
	}
	
	
}
