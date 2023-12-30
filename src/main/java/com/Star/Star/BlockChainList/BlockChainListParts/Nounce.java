package com.Star.Star.BlockChainList.BlockChainListParts;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.Star.Star.BlockChainList.services.RSAService;

public class Nounce implements Serializable {
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
	
	public JSONObject getJson() throws Exception {
		JSONObject json = new JSONObject();
		JSONArray pks = new JSONArray();
		for (int i = 0; i < publicKeys.size(); i++) {
			pks.put(RSAService.pkToString(publicKeys.get(i)));
		}
		JSONArray signatures = new JSONArray();
		for (int i = 0; i < hashSignatures.size(); i++) {
			signatures.put(hashSignatures.get(i));
		}
		json.put("Transaction Hash", this.hash);
		json.put("Peer Public Keys", pks);
		json.put("Signatures", signatures);
		json.put("Nounce", this.getNounce());
		return json;
	}
	
}
