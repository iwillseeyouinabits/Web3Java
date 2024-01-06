package com.Star.Star.BlockChainList.services;

import com.Star.Star.BlockChainList.BlockChainListParts.Block;
import com.Star.Star.BlockChainList.BlockChainListParts.CurrencyTransaction;
import com.Star.Star.BlockChainList.BlockChainListParts.HttpTransaction;
import com.Star.Star.BlockChainList.BlockChainListParts.DockerTransaction;
import com.Star.Star.BlockChainList.BlockChainListParts.TransactionPackage;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


import static com.Star.Star.BlockChainList.services.TransactionService.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer that contains validation logic for blocks and transactions
 */
public class ValidationService {

    public boolean validateTransaction(List<TransactionPackage> tps, TransactionPackage tp) {
        return !tps.contains(tp) && this.validateTransactionMetadata(tp);
    }

    public boolean validateTransactionMetadata(TransactionPackage transactionPackage) {
        boolean transactionRule;
        boolean hashRule;
        boolean signatureRule;
        try {
            //validating transaction signature
            signatureRule = transactionPackage.verifySigner();
            //validating transaction and hash for each type of transaction
            if (transactionPackage.getTransaction() instanceof HttpTransaction) {
                transactionRule = validateHttpTransactionMetadata(
                        (HttpTransaction)transactionPackage.getTransaction(), transactionPackage.getGassFee());
                hashRule = transactionPackage.getHash().equals(
                        getHTTPTransactionHash((HttpTransaction) transactionPackage.getTransaction()));
            }
            else if (transactionPackage.getTransaction() instanceof DockerTransaction) {
                transactionRule = validateDockerTransactionMetadata (
                        (DockerTransaction)transactionPackage.getTransaction(), transactionPackage.getGassFee());
                hashRule = transactionPackage.getHash().equals(
                        getDockerTransactionHash((DockerTransaction) transactionPackage.getTransaction()));
            }
            else {
                transactionRule = validateCurrencyTransactionMetadata (
                        (CurrencyTransaction) transactionPackage.getTransaction(), transactionPackage.getGassFee());
                hashRule = transactionPackage.getHash().equals(
                        getCurrencyTransactionHash((CurrencyTransaction) transactionPackage.getTransaction()));
            }
        } catch (Exception e) {
            System.err.println("%^^ " + e.getMessage());
            return false;
        }
        return transactionRule && hashRule && signatureRule;
    }

    public synchronized boolean validateBlock(Block block, String prevHash, List<TransactionPackage> onChainTransactions, ConcurrentHashMap<String, Boolean> verifiedBlocks) throws Exception {
        
        if (verifiedBlocks.get(block.getHash()) != null && verifiedBlocks.get(block.getHash())) {
            return true;
        }

        // System.out.println(prevHash + " " + block.getPrevHash() + " " + (block.getPrevHash().equals(prevHash)));
        if (!block.getPrevHash().equals(prevHash)) {
            return false;
        }
        List<TransactionPackage> validatedTransactionsInBlock = new ArrayList<TransactionPackage>();
        for (TransactionPackage tp : block.getTransactions()) {
            List<TransactionPackage> validatedTransactions = new ArrayList<TransactionPackage>();
            validatedTransactions.addAll(onChainTransactions);
            validatedTransactions.addAll(validatedTransactionsInBlock);
            if (!this.validateTransaction(validatedTransactions, tp)) {
                // System.out.println("Transaction Verify Fail: " + tp.getHash());
                return false;
            } else {
                validatedTransactionsInBlock.add(tp);
            }
        }
        return true;
    }

    public boolean validateBlockMetaData(Block block) throws Exception {
        boolean validate = true;
        if (!RSAService.verify(block.getHash(), block.getBlockSig(), block.getMinerPublicKey())) {
            validate = false;
        }
        for (int i = 0; i < block.getNounce().getPublicKeys().size(); i++) {
            if (!RSAService.verify(block.getNounce().getHash() + true, block.getNounce().getHashSignatures().get(i), block.getNounce().getPublicKeys().get(i))){
                validate = false;
                break;
            }
        }

        return validate;
    }

    public boolean validateHttpTransactionMetadata(HttpTransaction transaction, double gassFee) {
        boolean gassFeeRule;
        boolean jsonRule;
        //validating gass fee
        gassFeeRule = gassFee == calcHttpGassFee(transaction.getPostJson());
        //validating valid json
        jsonRule = this.isValidJson(transaction.getPostJson());
        return jsonRule && gassFeeRule;
    }

    public boolean validateCurrencyTransactionMetadata(CurrencyTransaction transaction, double gassFee) {
        boolean gassFeeRule;
        boolean minTokenRule;
        //validating gass fee
        gassFeeRule = gassFee == calcCurrencyGassFee();
        //validating valid json
        minTokenRule = transaction.getTokens() >= 0;
        return minTokenRule && gassFeeRule;
    }
    
    public boolean isValidJson(String json) {
    try {
        new JSONObject(json);
    } catch (JSONException e) {
        return false;
    }
    return true;
}

    public boolean validateDockerTransactionMetadata(DockerTransaction transaction, double gassFee) {
        //validating gass fee
        return gassFee == calcDockerGassFee(transaction.getDockerFile(), transaction.getArchiveBase64());
    }
}
