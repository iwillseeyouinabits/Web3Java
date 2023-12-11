package com.Star.Star;

import java.io.Serializable;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import org.json.JSONObject;

import com.Star.Star.services.RSAService;

import static com.Star.Star.services.TransactionService.getCurrencyTransactionHash;

/**
 * Currency Transaction
 */
public class CurrencyTransaction extends Transaction implements Serializable{

	final String uuid;
	final PublicKey senderAdr;
	final PublicKey recipientAdr;
	final int tokens;

	public CurrencyTransaction(PublicKey senderAdr, PublicKey recipientAdr, int tokens, String uuid) {
		super();
		this.senderAdr = senderAdr;
		this.recipientAdr = recipientAdr;
		this.tokens = tokens;
		this.uuid = uuid;
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



	public int getTokens() {
		return tokens;
	}


	public PublicKey getSenderAdr() { 
		return senderAdr; 
	}

	public PublicKey getRecipientAdr() { 
		return recipientAdr;
	}

	public String getUuid() {
		return uuid;
	}

	public JSONObject getJson() {
		JSONObject json = new JSONObject();
		json.put("Sender Address", RSAService.pkToString(this.senderAdr));
		json.put("Recipient Address", RSAService.pkToString(this.recipientAdr));
		json.put("Tokens", this.tokens);
		json.put("UUID", this.getUuid().toString());
		return json;
	}
}
