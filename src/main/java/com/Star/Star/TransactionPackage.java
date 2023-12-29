package com.Star.Star;

import com.Star.Star.services.RSAService;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.json.JSONException;
import org.json.JSONObject;

public class TransactionPackage implements Serializable {
	final double gassFee;
	final String hash;
	final String signature;
	final Transaction transaction;

	public TransactionPackage(Transaction transaction, PrivateKey signer) throws Exception {
		this.gassFee = transaction.byteSize()/1000000.0;
		this.hash = transaction.getHash();
		this.signature = RSAService.sign(this.hash, signer);
		this.transaction = transaction;
	}

	public boolean verifySigner() throws Exception {
		return RSAService.verify(this.hash, this.signature, this.transaction.getSigner());
	}

	public PublicKey getSigner() {
		PublicKey signer = this.transaction.getSigner();
		return signer;
	}

	public Transaction getTransaction() { 
		return transaction;
	}

	public double getGassFee() {
		return gassFee;
	}

	public String getHash() {
		return this.hash;
	}

	public String getUUID() {
		return transaction.getUuid();
	}
	
	@Override
	public boolean equals(Object o) {
		TransactionPackage rhs = (TransactionPackage) o;
		return this.hash.equals(rhs.getHash());
	}

	@Override
	public int hashCode(){
		return Integer.parseInt(this.getHash().substring(0, 7).toUpperCase(), 16);
	}
	
	public JSONObject getJson() throws JSONException, NoSuchAlgorithmException {
		JSONObject json = new JSONObject();
		String transactionType = "";
		if (transaction instanceof CurrencyTransaction) {
			transactionType = "Currency";
		} else if (transaction instanceof HttpTransaction) {
			transactionType = "Http";
		} else if (transaction instanceof ShellTransaction) {
			transactionType = "Shell";
		} 
		json.put("Transaction Type", transactionType);
		json.put("Signature", this.signature);
		json.put("Hash", this.transaction.getHash());
		json.put("Body", this.transaction.getJson());
		return json;
	}
}
