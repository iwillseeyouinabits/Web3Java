package com.Star.Star;

import java.io.Serializable;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static com.Star.Star.services.TransactionService.getHTTPTransactionHash;

/**
 * HttpTransaction
 */
public class HttpTransaction extends Transaction implements Serializable {

	final PublicKey clientAdr;
	final PublicKey websiteAdr;
	final PublicKey hostAdr;
	final String postJson;
	
	public HttpTransaction(PublicKey clientAdr, PublicKey websiteAdr, PublicKey hostAdr, String postJson) {
		super();
		this.clientAdr = clientAdr;
		this.websiteAdr = websiteAdr;
		this.hostAdr = hostAdr;
		this.postJson = postJson;
	}
	
	public HttpTransaction(HttpTransaction rhs) throws InvalidKeySpecException, NoSuchAlgorithmException {
		this.clientAdr = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rhs.clientAdr.getEncoded()));
		this.websiteAdr = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rhs.websiteAdr.getEncoded()));
		this.hostAdr = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rhs.hostAdr.getEncoded()));
		this.postJson = rhs.postJson;
	}
	
	
	@Override
	public long byteSize() {
		return 64*3+this.postJson.length();
	}

	@Override
	public PublicKey getSigner() {
		return this.clientAdr;
	}

	@Override
	public String getHash() throws NoSuchAlgorithmException {
		return getHTTPTransactionHash(this);
	}
	
	public Transaction getDeepCopy() throws InvalidKeySpecException, NoSuchAlgorithmException {
		return new HttpTransaction(this);
	}

	public String getPostJson() { return this.postJson;}

	public PublicKey getClientAdr() {
		return clientAdr;
	}

	public PublicKey getWebsiteAdr() {
		return websiteAdr;
	}

	public PublicKey getHostAdr() {
		return hostAdr;
	}

}
