package com.Star.Star;

import java.security.PrivateKey;
import java.security.PublicKey;

public class TransactionPackage {
	final double gass_fee;
	final String hash;
	final String signature;
	final TransactionBody transactionBody;
	
	public TransactionPackage(TransactionBody transactionBody, PrivateKey signer) throws Exception {
		this.gass_fee = transactionBody.byteSize()/1000000.0;
		this.hash = transactionBody.getHash();
		this.signature = new RSA().sign(this.hash+"", signer);
		this.transactionBody = transactionBody;
	}
	
	public boolean verifySigner() throws Exception {
		PublicKey signer = this.transactionBody.getSigner();
		return new RSA().verify(this.hash+"", this.signature, signer);
	}
	
	public PublicKey getSigner() {
		PublicKey signer = this.transactionBody.getSigner();
		return signer;
	}
	
	public String getHash() {
		return this.hash;
	}
}
