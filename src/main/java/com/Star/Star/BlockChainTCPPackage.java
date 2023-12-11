package com.Star.Star;


import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.Star.Star.services.RSAService;

public class BlockChainTCPPackage implements Serializable {

    private List<Block> blockChainList;

    public BlockChainTCPPackage(Map<String, Block> blockChain) throws Exception {
        this.blockChainList = Collections.synchronizedList(this.getBlockChainList(blockChain));
    }

    public Map<String, Block> getBlockChain() throws NoSuchAlgorithmException {
        Map<String, Block> bc = new HashMap<String, Block>();
        String prevHash = "000000000000000";
        for (Block block : this.blockChainList) {
            bc.put(prevHash, block);
            prevHash = block.getHash();
        }
        return bc;
    }

	private ArrayList<Block> getBlockChainList(Map<String, Block> bc) throws Exception {
		ArrayList<Block> blockList = new ArrayList<Block>();
		String prevHash = "000000000000000";
		while (bc.containsKey(prevHash)) {
			Block curBlock = bc.get(prevHash);
			prevHash = curBlock.getHash();
			blockList.add(curBlock);
		}
		return blockList;
	}
 
	public List<Block> getBlockChainList() {
		return this.blockChainList;
	}

    public String getEntireHashOfBlockChain() throws NoSuchAlgorithmException {
		Map<String, Block> bc = getBlockChain();
        String prevHash = "000000000000000";
		String hashes = prevHash;
		while (bc.containsKey(prevHash)) {
			Block curBlock = bc.get(prevHash);
			prevHash = curBlock.getHash();
			hashes += prevHash;
		}
		return new RSAService().getSHA256(hashes);
	}

    

	public List<TransactionPackage> getTransactions() throws NoSuchAlgorithmException {
		List<TransactionPackage> ts = Collections.synchronizedList(new ArrayList<TransactionPackage>());
        Map<String, Block> bc = getBlockChain();
		for (Entry<String, Block> b : bc.entrySet()) {
			for (TransactionPackage t : b.getValue().getTransactions()) {
				ts.add(t);
			}
		}
		return ts;
	}

}