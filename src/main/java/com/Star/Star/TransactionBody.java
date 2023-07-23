package com.Star.Star;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public abstract class TransactionBody {
	public abstract long byteSize();
	public abstract PublicKey getSigner();;
	public abstract String getHash() throws NoSuchAlgorithmException;
}
