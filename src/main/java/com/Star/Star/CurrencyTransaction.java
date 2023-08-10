package com.Star.Star;

import java.io.Serializable;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import static com.Star.Star.services.TransactionService.getCurrencyTransactionHash;

/**
 * Currency Transaction
 */
public class CurrencyTransaction extends Transaction implements Serializable{

	final PublicKey senderAdr;
	final PublicKey recipientAdr;
	final int tokens;

	public CurrencyTransaction(PublicKey senderAdr, PublicKey recipientAdr, int tokens) {
		super();
		this.senderAdr = senderAdr;
		this.recipientAdr = recipientAdr;
		this.tokens = tokens;
	}
	
	public CurrencyTransaction(CurrencyTransaction rhs) throws InvalidKeySpecException, NoSuchAlgorithmException {
		this.senderAdr = KeyFactory.getInstance("RSA").generatePublic(
				new X509EncodedKeySpec(rhs.senderAdr.getEncoded()));
		this.recipientAdr = KeyFactory.getInstance("RSA").generatePublic(
				new X509EncodedKeySpec(rhs.recipientAdr.getEncoded()));
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
		return getCurrencyTransactionHash(this);
	}


	public Transaction getDeepCopy() throws InvalidKeySpecException, NoSuchAlgorithmException {
		return new CurrencyTransaction(this);
	}

	public int getTokens() {
		return tokens;
	}


	public PublicKey getSenderAdr() { return senderAdr; }

	public PublicKey getRecipientAdr() { return recipientAdr; }
}
