package com.Star.Star.BlockChainList.BlockChainListParts;

import static com.Star.Star.BlockChainList.services.TransactionService.getDockerTransactionHash;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import org.json.JSONObject;

import com.Star.Star.BlockChainList.services.RSAService;

/**
 * Docker Transaction
 */
public class DockerTransaction extends Transaction implements Serializable {

	final String uuid;
	final PublicKey websiteAdr;
	final String dockerFile;
	final String archiveBase64;
	
	public DockerTransaction(PublicKey websiteAdr, String dockerFile, String archiveBase64, String uuid) {
		super();
		this.websiteAdr = websiteAdr;
		this.dockerFile = dockerFile;
		this.archiveBase64 = archiveBase64;
		this.uuid = uuid;
	}	
	
	@Override
	public long byteSize() {
		return 64*3 + this.dockerFile.length() + this.archiveBase64.length();
	}

	@Override
	public PublicKey getSigner() {
		return this.websiteAdr;
	}
	
	public String getHash() throws NoSuchAlgorithmException {
		return getDockerTransactionHash(this);
	}

	public String getDockerFile() {return dockerFile;}

	public String getArchiveBase64() {return archiveBase64;}

	public PublicKey getWebsiteAdr() {return websiteAdr;}
	
	public String getUuid() {
		return uuid;
	}

	public JSONObject getJson() {
		JSONObject json = new JSONObject();
		json.put("Website Address", RSAService.pkToString(this.websiteAdr));
		json.put("Docker File", this.dockerFile);
		json.put("Archive Base64", this.archiveBase64);
		json.put("UUID", this.getUuid().toString());
		return json;
	}
}
