package com.Star.Star;

import java.io.Serializable;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

public class CurrencyTransaction extends Transaction implements Serializable{

	final PublicKey senderAdr;
	final PublicKey recipientAdr;
	final int tokens;
	UUID uuid;
	
	public CurrencyTransaction(PublicKey senderAdr, PublicKey recipientAdr, int tokens) {
		this.senderAdr = senderAdr;
		this.recipientAdr = recipientAdr;
		this.tokens = tokens;
		this.uuid =  new UUID((long) (Math.random()*new Long(0).MAX_VALUE), (long) (Math.random()*new Long(0).MAX_VALUE));
	}
	
	public CurrencyTransaction(CurrencyTransaction rhs) throws InvalidKeySpecException, NoSuchAlgorithmException {
		this.senderAdr = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rhs.senderAdr.getEncoded()));
		this.recipientAdr = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rhs.recipientAdr.getEncoded()));;
		this.tokens = rhs.tokens;
		this.uuid =  new UUID(rhs.uuid.getMostSignificantBits(), rhs.uuid.getLeastSignificantBits());
	}
	
	
	@Override
	public long byteSize() {
		return 64;
	}

	@Override
	public PublicKey getSigner() {
		return this.senderAdr;
	}


	@Override
	public String getHash() throws NoSuchAlgorithmException {
		return new RSA().getSHA256(new RSA().pkToString(this.senderAdr)+new RSA().pkToString(this.recipientAdr)+tokens+uuid.toString());
	}


	public Transaction getDeepCopy() throws InvalidKeySpecException, NoSuchAlgorithmException {
		return new CurrencyTransaction(this);
	}

	
}
