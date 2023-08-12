package com.Star.Star.services;

import com.Star.Star.*;
import org.json.JSONObject;
import org.json.JSONTokener;

import static com.Star.Star.services.DateService.getCurrentTime;
import static com.Star.Star.services.TransactionService.*;

/**
 * Service layer that contains validation logic for blocks and transactions
 */
public class ValidationService {

    public static boolean validate(Block latestBlock, Block blockToVerify) {
        boolean blockSignatureRule;
        boolean prevBlockHashRule;
        boolean blockTimeRule;
        boolean transactionsRule;
        try {
            //validating Block Signature
            blockSignatureRule = RSAService.verify(
                    blockToVerify.getHash(), blockToVerify.getBlockSig(), blockToVerify.getBlockBody().getMinerPk());
            //validating that the new blocks previous hash is correct
            prevBlockHashRule = blockToVerify.getBlockBody().getPrevBlockHash().equals(latestBlock.getHash());
            //validating time on new block
            long latestBlockTime = latestBlock.getBlockBody().getTimestamp();
            long blockToVerifyTime = blockToVerify.getBlockBody().getTimestamp();
            blockTimeRule = latestBlockTime <= blockToVerifyTime || latestBlockTime > getCurrentTime();
            //validating all transactions on block
            transactionsRule = blockToVerify.getTransactions().stream().allMatch(ValidationService::validate);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return blockSignatureRule && blockTimeRule && prevBlockHashRule && transactionsRule;
    }

    public static boolean validate(TransactionPackage transactionPackage) {
        boolean transactionRule;
        boolean hashRule;
        boolean signatureRule;
        try {
            //validating transaction signature
            signatureRule = transactionPackage.verifySigner();
            //validating transaction and hash for each type of transaction
            if (transactionPackage.getTransaction() instanceof HttpTransaction) {
                transactionRule = validate(
                        (HttpTransaction)transactionPackage.getTransaction(), transactionPackage.getGassFee());
                hashRule = transactionPackage.getHash().equals(
                        getHTTPTransactionHash((HttpTransaction) transactionPackage.getTransaction()));
            }
            else if (transactionPackage.getTransaction() instanceof ShellTransaction) {
                transactionRule = validate(
                        (ShellTransaction)transactionPackage.getTransaction(), transactionPackage.getGassFee());
                hashRule = transactionPackage.getHash().equals(
                        getShellTransactionHash((ShellTransaction) transactionPackage.getTransaction()));
            }
            else {
                transactionRule = validate(
                        (CurrencyTransaction) transactionPackage.getTransaction(), transactionPackage.getGassFee());
                hashRule = transactionPackage.getHash().equals(
                        getCurrencyTransactionHash((CurrencyTransaction) transactionPackage.getTransaction()));
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return false;
        }
        return transactionRule && hashRule && signatureRule;
    }

    public static boolean validate(HttpTransaction transaction, double gassFee) {
        boolean gassFeeRule;
        boolean jsonRule;
        //validating gass fee
        gassFeeRule = gassFee == calcHttpGassFee(transaction.getPostJson());
        //validating valid json
        jsonRule = new JSONTokener(transaction.getPostJson()).nextValue() instanceof JSONObject;
        return jsonRule && gassFeeRule;
    }

    public static boolean validate(CurrencyTransaction transaction, double gassFee) {
        boolean gassFeeRule;
        boolean minTokenRule;
        //validating gass fee
        gassFeeRule = gassFee == calcCurrencyGassFee();
        //validating valid json
        minTokenRule = transaction.getTokens() >= 0;
        return minTokenRule && gassFeeRule;
    }

    public static boolean validate(ShellTransaction transaction, double gassFee) {
        //validating gass fee
        return gassFee == calcShellGassFee(transaction.getShell(), transaction.getWebsite_name());
    }
}
