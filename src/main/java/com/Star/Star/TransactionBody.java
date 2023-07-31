package com.Star.Star;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.UUID;

public abstract class TransactionBody {
	private UUID uuid;
	public abstract long byteSize();
	public abstract PublicKey getSigner();;
	public abstract String getHash() throws NoSuchAlgorithmException;
}
