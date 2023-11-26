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

import static com.Star.Star.services.TransactionService.getShellTransactionHash;

/**
 * ShellTransaction
 */
public class ShellTransaction extends Transaction implements Serializable {

	final String uuid;
	final PublicKey websiteAdr;
	final String shell;
	final String website_name;
	
	public ShellTransaction(PublicKey websiteAdr, String shell, String website_name, String uuid) {
		super();
		this.websiteAdr = websiteAdr;
		this.shell = shell;
		this.website_name = website_name;
		this.uuid = uuid;
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

	public String getShell() {return shell;}

	public String getWebsite_name() {return website_name;}

	public PublicKey getWebsiteAdr() {return websiteAdr;}
	
	public String getUuid() {
		return uuid;
	}

	public JSONObject getJson() {
		JSONObject json = new JSONObject();
		json.put("Website Address", RSAService.pkToString(this.websiteAdr));
		json.put("Shell Script", this.shell);
		json.put("UUID", this.getUuid().toString());
		return json;
	}
}
