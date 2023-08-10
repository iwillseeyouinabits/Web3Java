package com.Star.Star;

import java.io.Serializable;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.UUID;

import static com.Star.Star.services.TransactionService.getShellTransactionHash;

/**
 * ShellTransaction
 */
public class ShellTransaction extends Transaction implements Serializable {

	final PublicKey websiteAdr;
	final String shell;
	final String website_name;
	
	public ShellTransaction(PublicKey websiteAdr, String shell, String website_name) {
		super();
		this.websiteAdr = websiteAdr;
		this.shell = shell;
		this.website_name = website_name;
	}
	
	public ShellTransaction(ShellTransaction rhs) throws InvalidKeySpecException, NoSuchAlgorithmException {
		this.websiteAdr = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rhs.websiteAdr.getEncoded()));
		this.shell = rhs.shell;
		this.website_name = rhs.website_name;
		uuid = new UUID(rhs.uuid.getMostSignificantBits(), rhs.uuid.getLeastSignificantBits());
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
		return getShellTransactionHash(this);
	}
	

	public Transaction getDeepCopy() throws InvalidKeySpecException, NoSuchAlgorithmException {
		return new ShellTransaction(this);
	}

	public String getShell() {return shell;}

	public String getWebsite_name() {return website_name;}

	public PublicKey getWebsiteAdr() {return websiteAdr;}
}
