package com.Star.Star;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

public abstract class Transaction {
	Transaction() {
		uuid = UUID.randomUUID();
	}
	protected UUID uuid;
	public abstract long byteSize();
	public abstract PublicKey getSigner();
	public abstract String getHash() throws NoSuchAlgorithmException;
	public abstract Transaction getDeepCopy() throws InvalidKeySpecException, NoSuchAlgorithmException;
	public UUID getUuid() {return uuid;}
}
