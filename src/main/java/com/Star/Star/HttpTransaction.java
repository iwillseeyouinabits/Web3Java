package com.Star.Star;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.UUID;

public class HttpTransaction extends TransactionBody  implements Serializable {

	final PublicKey clientAdr;
	final PublicKey websiteAdr;
	final PublicKey hostAdr;
	final String postJson;
	UUID uuid;
	
	public HttpTransaction(PublicKey clientAdr, PublicKey websiteAdr, PublicKey hostAdr, String postJson) {
		this.clientAdr = clientAdr;
		this.websiteAdr = websiteAdr;
		this.hostAdr = hostAdr;
		this.postJson = postJson;
		uuid = new UUID((long) (Math.random()*new Long(0).MAX_VALUE), (long) (Math.random()*new Long(0).MAX_VALUE));
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
		return new RSA().getSHA256(new RSA().pkToString(this.clientAdr)+new RSA().pkToString(this.websiteAdr)+new RSA().pkToString(this.hostAdr)+this.postJson+uuid.toString());
	}

}
