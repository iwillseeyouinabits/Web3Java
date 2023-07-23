package com.Star.Star;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class CurrencyTransaction extends TransactionBody {

	final PublicKey senderAdr;
	final PublicKey recipientAdr;
	final int tokens;
	
	public CurrencyTransaction(PublicKey senderAdr, PublicKey recipientAdr, int tokens) {
		this.senderAdr = senderAdr;
		this.recipientAdr = recipientAdr;
		this.tokens = tokens;
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
		return new RSA().getSHA256(new RSA().pkToString(this.senderAdr)+new RSA().pkToString(this.recipientAdr)+tokens);
	}

}
