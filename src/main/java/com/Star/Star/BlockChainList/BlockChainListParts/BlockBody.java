package com.Star.Star.BlockChainList.BlockChainListParts;

import static com.Star.Star.BlockChainList.services.DateService.getCurrentTime;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.Star.Star.BlockChainList.services.RSAService;

public class BlockBody implements Serializable {
	final String prevBlockHash;
	final PublicKey minerPk;
	private String nounce;
	long timestamp;
	List<TransactionPackage> block;
	String hashableToken;

	public String getPrevBlockHash() {return prevBlockHash;}

	public long getTimestamp() {return timestamp;}

	public PublicKey getMinerPk() {return minerPk;}

	public BlockBody(String prevBlockHash, PublicKey minerPk) throws NoSuchAlgorithmException {
		this.prevBlockHash = prevBlockHash;
		this.minerPk = minerPk;
		this.timestamp = getCurrentTime();
		this.block = Collections.synchronizedList(new ArrayList<TransactionPackage>());
		this.hashableToken = this.prevBlockHash + RSAService.getSHA256(RSAService.pkToString(this.minerPk));
	}

	public void addTransaction(TransactionPackage transaction) throws NoSuchAlgorithmException {
		this.hashableToken = RSAService.getSHA256(this.hashableToken + transaction.getHash());
		block.add(transaction);
	}

	public String getHash() throws Exception { 
		return RSAService.getSHA256(getHashableToken());
	}

	public void setNounce(String n) {
		this.nounce = n;
	}
	
	public JSONObject getJson() throws Exception {
		JSONObject json = new JSONObject();
		JSONArray transactions = new JSONArray();
		for (int i = 0; i < block.size(); i++) {
			transactions.put(block.get(i).getJson());
		}
		json.put("Previous Block Hash", this.prevBlockHash);
		json.put("Miner Address", RSAService.pkToString(this.minerPk));
		json.put("Hash", this.getHash());
		json.put("Nounce", this.nounce);
		json.put("Transactions", transactions);
		return json;
	}
	
	private String getHashableToken() throws Exception {
		// if (this.nounce.length() == 0) {
		// 	throw new Exception("Nounce Not Defined: Cannot Get Hash Of Block");
		// }
		return this.nounce + this.hashableToken;
	}

}
