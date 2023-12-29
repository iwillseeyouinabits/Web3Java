package com.Star.Star.BlockChainList;

import java.io.Serializable;
import java.security.PublicKey;

public class TCPResponse implements Serializable{
    private PublicKey pk;
    private String hash;
    private String hashSignature;

    public TCPResponse(PublicKey pk, String hash, String hashSignature) {
        this.pk = pk;
        this.hash = hash;
        this.hashSignature = hashSignature;
    }

    public PublicKey getPublicKey() {
        return pk;
    }

    public String getHash() {
        return hash;
    }

    public String getHashSignature() {
        return hashSignature;
    }
}
