package com.Star.Star.BlockChainList;

import java.io.Serializable;
import java.security.PublicKey;

public class ServerAddress implements Serializable {
	private String ip;
	private int port;
	private PublicKey pk;
	
	public ServerAddress(String ip, int port, PublicKey pk) {
		this.ip = ip;
		this.port = port;
		this.pk = pk;
	}
	
	public String getIp() {
		return this.ip;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public PublicKey getPublicKey() {
		return this.pk;
	}
	
}
