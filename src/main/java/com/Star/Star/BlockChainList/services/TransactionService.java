package com.Star.Star.BlockChainList.services;

import java.security.NoSuchAlgorithmException;

import com.Star.Star.BlockChainList.BlockChainListParts.CurrencyTransaction;
import com.Star.Star.BlockChainList.BlockChainListParts.HttpTransaction;
import com.Star.Star.BlockChainList.BlockChainListParts.ShellTransaction;

/**
 * Service layer that contains business logic for transaction
 */
public class TransactionService {
    public static double calcHttpGassFee(String postJson) {
        return (64*3 + postJson.length())/1000000.0;
    }
    public static double calcShellGassFee(String shellScript, String websiteName) {
        return (64*3 + shellScript.length() + websiteName.length())/1000000.0;
    }
    public static double calcCurrencyGassFee() {
        return 64/1000000.0;
    }

    public static String getCurrencyTransactionHash(CurrencyTransaction transaction) throws NoSuchAlgorithmException {
        return RSAService.getSHA256(RSAService.pkToString(
                transaction.getSenderAdr()) + RSAService.pkToString(
                transaction.getRecipientAdr())+transaction.getTokens() + transaction.getUuid().toString());
    }

    public static String getHTTPTransactionHash(HttpTransaction transaction) throws NoSuchAlgorithmException {
        return RSAService.getSHA256(RSAService.pkToString(transaction.getClientAdr())
                + RSAService.pkToString(transaction.getWebsiteAdr())
                + RSAService.pkToString(transaction.getHostAdr())
                +transaction.getPostJson()+transaction.getUuid().toString());
    }

    public static String getShellTransactionHash(ShellTransaction transaction) throws NoSuchAlgorithmException {
        return RSAService.getSHA256(transaction.getShell()
                + transaction.getWebsite_name()
                + RSAService.pkToString(transaction.getWebsiteAdr())
                + transaction.getUuid().toString());
    }
}
