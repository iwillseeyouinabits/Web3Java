package com.Star.Star.BlockChainList.BlockChainListParts;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

import com.Star.Star.BlockChainList.services.RSAService;

public class TCPResponse implements Serializable{
    private PublicKey pk;
    private String hash;
    private boolean guarantee;
    private String hashSignature;

    public TCPResponse(PublicKey pk, PrivateKey sk, String hash, boolean guarantee) throws Exception {
        this.pk = pk;
        this.hash = hash;
        this.guarantee = guarantee;
        this.hashSignature = RSAService.sign((hash + guarantee), sk);
    }

    public PublicKey getPublicKey() {
        return pk;
    }

    public String getHash() {
        return hash;
    }

    public boolean getGuarantee() {
        return this.guarantee;
    }

    public String getHashSignature() {
        return hashSignature;
    }
}
