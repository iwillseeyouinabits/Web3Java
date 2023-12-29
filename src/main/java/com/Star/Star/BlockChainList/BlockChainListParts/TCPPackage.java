package com.Star.Star.BlockChainList.BlockChainListParts;

import java.io.Serializable;

/**
 * Wrapper class to allow for sending both transactions and blocks
 */
public abstract class TCPPackage implements Serializable {
	
	public abstract Object getPackage ();
	
	public abstract String getHash();

}
