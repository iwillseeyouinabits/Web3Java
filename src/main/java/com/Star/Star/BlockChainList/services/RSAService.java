package com.Star.Star.BlockChainList.services;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * Service layer that contains RSA encryption logic
 */
public class RSAService {

	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
	    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
	    generator.initialize(2048, new SecureRandom());
	    return generator.generateKeyPair();
	}
	
	public static String pkToString(PublicKey pk) {
		return Hex.encodeHexString(pk.getEncoded());
	}
	
	public static String skToString(PrivateKey sk) {
		return Hex.encodeHexString(sk.getEncoded());
	}
	
	public static PublicKey stringToPk(String pkString) throws InvalidKeySpecException, NoSuchAlgorithmException, DecoderException {
		byte[] byteKey = Hex.decodeHex(pkString);
		X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(X509publicKey);
	}
	
	public static PrivateKey stringToSk(String skString) throws InvalidKeySpecException, NoSuchAlgorithmException, DecoderException {
		byte[] byteKey = Hex.decodeHex(skString);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(new PKCS8EncodedKeySpec(byteKey));
	}
	
	public String encrypt(String plainText, PublicKey publicKey) throws Exception {
	    Cipher encryptCipher = Cipher.getInstance("RSA");
	    encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

	    byte[] cipherText = encryptCipher.doFinal(plainText.getBytes());

	    return Hex.encodeHexString(cipherText);
	}
	
	public String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
	    byte[] bytes = Hex.decodeHex(cipherText);

	    Cipher decriptCipher = Cipher.getInstance("RSA");
	    decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

	    return new String(decriptCipher.doFinal(bytes));
	}
	
	public static String sign(String plainText, PrivateKey privateKey) throws Exception {
	    Signature privateSignature = Signature.getInstance("SHA256withRSA");
	    privateSignature.initSign(privateKey);
	    privateSignature.update(plainText.getBytes());

	    byte[] signature = privateSignature.sign();

	    return Hex.encodeHexString(signature);
	}
	
	public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
	    Signature publicSignature = Signature.getInstance("SHA256withRSA");
	    publicSignature.initVerify(publicKey);
	    publicSignature.update(plainText.getBytes());

	    byte[] signatureBytes = Hex.decodeHex(signature);

	    return publicSignature.verify(signatureBytes);
	}
	
    public static String getSHA256(String input) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

        BigInteger number = new BigInteger(1, hash);

        StringBuilder hexString = new StringBuilder(number.toString(16));
 
        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }
 
        return hexString.toString();
    }
	
}
