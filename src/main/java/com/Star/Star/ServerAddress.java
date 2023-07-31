package com.Star.Star;

public class ServerAddress {
	private String ip;
	private int port;
	
	public ServerAddress(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	public String getIp() {
		return this.ip;
	}
	
	public int getPort() {
		return this.port;
	}
	
}
