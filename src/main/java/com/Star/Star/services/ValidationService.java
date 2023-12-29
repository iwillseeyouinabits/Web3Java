package com.Star.Star.services;

import com.Star.Star.*;
import org.json.JSONObject;
import org.json.JSONTokener;

import static com.Star.Star.services.DateService.getCurrentTime;
import static com.Star.Star.services.TransactionService.*;

import java.util.List;

/**
 * Service layer that contains validation logic for blocks and transactions
 */
public class ValidationService {

    public boolean validate(List<TransactionPackage> tps, TransactionPackage tp) {
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
            else if (transactionPackage.getTransaction() instanceof ShellTransaction) {
                transactionRule = validateShellTransactionMetadata (
                        (ShellTransaction)transactionPackage.getTransaction(), transactionPackage.getGassFee());
                hashRule = transactionPackage.getHash().equals(
                        getShellTransactionHash((ShellTransaction) transactionPackage.getTransaction()));
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

    public boolean validateHttpTransactionMetadata(HttpTransaction transaction, double gassFee) {
        boolean gassFeeRule;
        boolean jsonRule;
        //validating gass fee
        gassFeeRule = gassFee == calcHttpGassFee(transaction.getPostJson());
        //validating valid json
        jsonRule = new JSONTokener(transaction.getPostJson()).nextValue() instanceof JSONObject;
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

    public boolean validateShellTransactionMetadata(ShellTransaction transaction, double gassFee) {
        //validating gass fee
        return gassFee == calcShellGassFee(transaction.getShell(), transaction.getWebsite_name());
    }
}
