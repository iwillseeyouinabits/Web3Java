package com.Star.Star;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class ShellTransaction extends TransactionBody {

	final PublicKey websiteAdr;
	final String shell;
	final String website_name;
	
	public ShellTransaction(PublicKey websiteAdr, String shell, String website_name) {
		this.websiteAdr = websiteAdr;
		this.shell = shell;
		this.website_name = website_name;
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
		return new RSA().getSHA256(this.shell+this.website_name+new RSA().pkToString(this.websiteAdr));
	}

}
