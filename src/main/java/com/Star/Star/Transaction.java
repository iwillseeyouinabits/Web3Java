package com.Star.Star;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import org.json.JSONObject;

public abstract class Transaction {
	public abstract long byteSize();
	public abstract PublicKey getSigner();
	public abstract JSONObject getJson();
	public abstract String getHash() throws NoSuchAlgorithmException;
}
