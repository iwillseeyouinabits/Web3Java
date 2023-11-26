package com.Star.Star;

import java.io.Serializable;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import org.json.JSONObject;

import com.Star.Star.services.RSAService;

import static com.Star.Star.services.TransactionService.getHTTPTransactionHash;

/**
 * HttpTransaction
 */
public class HttpTransaction extends Transaction implements Serializable {

	final String uuid;
	final PublicKey clientAdr;
	final PublicKey websiteAdr;
	final PublicKey hostAdr;
	final String postJson;
	
	public HttpTransaction(PublicKey clientAdr, PublicKey websiteAdr, PublicKey hostAdr, String postJson, String uuid) {
		super();
		this.clientAdr = clientAdr;
		this.websiteAdr = websiteAdr;
		this.hostAdr = hostAdr;
		this.postJson = postJson;
		this.uuid = uuid;
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
		return getHTTPTransactionHash(this);
	}

	public String getPostJson() { return this.postJson;}

	public PublicKey getClientAdr() {
		return clientAdr;
	}

	public PublicKey getWebsiteAdr() {
		return websiteAdr;
	}

	public PublicKey getHostAdr() {
		return hostAdr;
	}

	public String getUuid() {
		return uuid;
	}

	public JSONObject getJson() {
		JSONObject json = new JSONObject();
		json.put("Client Address", RSAService.pkToString(this.clientAdr));
		json.put("Website Address", RSAService.pkToString(this.websiteAdr));
		json.put("Host Address", RSAService.pkToString(this.hostAdr));
		json.put("UUID", this.getUuid().toString());
		return json;
	}

}
