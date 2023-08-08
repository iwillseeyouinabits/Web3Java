package com.Star.Star;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

public class TransactionPackage implements Serializable {
	final double gass_fee;
	final String hash;
	final String signature;
	final Transaction transaction;

	public TransactionPackage(Transaction transaction, PrivateKey signer) throws Exception {
		this.gass_fee = transaction.byteSize()/1000000.0;
		this.hash = transaction.getHash();
		this.signature = new RSA().sign(this.hash+"", signer);
		this.transaction = transaction;
	}

	public TransactionPackage(TransactionPackage rhs) throws Exception {
		this.gass_fee = rhs.gass_fee;
		this.hash = rhs.hash + "";
		this.signature = rhs.signature + "";
		this.transaction = rhs.transaction.getDeepCopy();
	}

	public boolean verifySigner() throws Exception {
		PublicKey signer = this.transaction.getSigner();
		return RSA.verify(this.hash + "", this.signature, signer);
	}

	public PublicKey getSigner() {
		PublicKey signer = this.transaction.getSigner();
		return signer;
	}
	
	public String getHash() {
		return this.hash;
	}
	
	public boolean equals(Object o) {
		TransactionPackage rhs = (TransactionPackage) o;
		return this.hash.equals(rhs.getHash());
	}
}
