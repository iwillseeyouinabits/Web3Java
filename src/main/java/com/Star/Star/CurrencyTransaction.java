package com.Star.Star;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.UUID;

public class CurrencyTransaction extends TransactionBody  implements Serializable{

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

}
