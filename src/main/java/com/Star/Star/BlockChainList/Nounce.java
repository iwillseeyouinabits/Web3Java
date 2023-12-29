package com.Star.Star.BlockChainList;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;

import com.Star.Star.BlockChainList.services.RSAService;

public class Nounce {
    private ArrayList<String> hashSignatures;
    private ArrayList<PublicKey> publicKeys;
    private String hash;

    public Nounce(ArrayList<String> hashSignatures, ArrayList<PublicKey> publicKeys, String hash) {
        this.hashSignatures = hashSignatures;
        this.publicKeys = publicKeys;
        this.hash = hash;
        sortPublicKeysAndSignatures();
    }

    private void sortPublicKeysAndSignatures() {
        for (int i = 0; i < publicKeys.size(); i++) {
            for (int j = 0; j < publicKeys.size()-1; j++) {
                if (hashSignatures.get(j).compareTo(hashSignatures.get(j+1)) < 0) {
                    String tempSignature = hashSignatures.get(j);
                    PublicKey tempPublicKey = publicKeys.get(j);
                    hashSignatures.set(j, hashSignatures.get(j+1));
                    publicKeys.set(j, publicKeys.get(j+1));
                    hashSignatures.set(j+1, tempSignature);
                    publicKeys.set(j+1, tempPublicKey);
                }
            }
        }
    }

    public String getNounce() throws NoSuchAlgorithmException {
        String sigs = "";
        for (String sig : this.hashSignatures) {
            sigs += sig;
        }
        return RSAService.getSHA256(sigs);
    }
}
