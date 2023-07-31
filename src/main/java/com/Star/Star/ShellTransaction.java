package com.Star.Star;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.UUID;

public class ShellTransaction extends TransactionBody  implements Serializable {

	final PublicKey websiteAdr;
	final String shell;
	final String website_name;
	UUID uuid;
	
	public ShellTransaction(PublicKey websiteAdr, String shell, String website_name) {
		this.websiteAdr = websiteAdr;
		this.shell = shell;
		this.website_name = website_name;
		uuid =  new UUID((long) (Math.random()*new Long(0).MAX_VALUE), (long) (Math.random()*new Long(0).MAX_VALUE));
	}
	
	
	@Override
	public long byteSize() {
		return 64*3 + this.shell.length() + this.website_name.length();
	}

	@Override
	public PublicKey getSigner() {
		return this.websiteAdr;
	}
	
	public String getHash() throws NoSuchAlgorithmException {
		return new RSA().getSHA256(this.shell+this.website_name+new RSA().pkToString(this.websiteAdr)+uuid.toString());
	}

}
