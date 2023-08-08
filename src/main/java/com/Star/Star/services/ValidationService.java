package com.Star.Star.services;

import com.Star.Star.Block;
import com.Star.Star.RSA;
import com.Star.Star.TransactionPackage;

public class ValidationService {

    public boolean validate(Block block) {
        try {
            //validating Block Signature
            RSA.verify(block.getHash(), block.getBlockSig(), block.getBlockBody().getMinerPk());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    public void validate(TransactionPackage transaction) {

    }
}
