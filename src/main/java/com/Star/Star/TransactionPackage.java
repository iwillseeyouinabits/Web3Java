package com.Star.Star;

import com.Star.Star.services.RSAService;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

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

	public TransactionPackage(TransactionPackage rhs) throws Exception {
		this.gassFee = rhs.gassFee;
		this.hash = rhs.hash;
		this.signature = rhs.signature;
		this.transaction = rhs.transaction.getDeepCopy();
	}

	public boolean verifySigner() throws Exception {
		return RSAService.verify(this.hash, this.signature, this.transaction.getSigner());
	}

	public PublicKey getSigner() {
		PublicKey signer = this.transaction.getSigner();
		return signer;
	}

	public Transaction getTransaction() { return transaction;}

	public double getGassFee() {return gassFee;}

	public String getHash() {
		return this.hash;
	}
	
	public boolean equals(Object o) {
		TransactionPackage rhs = (TransactionPackage) o;
		return this.hash.equals(rhs.getHash());
	}
}
