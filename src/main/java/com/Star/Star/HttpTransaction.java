package com.Star.Star;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class HttpTransaction extends TransactionBody {

	final PublicKey clientAdr;
	final PublicKey websiteAdr;
	final PublicKey hostAdr;
	final String postJson;
	
	public HttpTransaction(PublicKey clientAdr, PublicKey websiteAdr, PublicKey hostAdr, String postJson) {
		this.clientAdr = clientAdr;
		this.websiteAdr = websiteAdr;
		this.hostAdr = hostAdr;
		this.postJson = postJson;
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
		return new RSA().getSHA256(new RSA().pkToString(this.clientAdr)+new RSA().pkToString(this.websiteAdr)+new RSA().pkToString(this.hostAdr)+this.postJson);
	}

}
