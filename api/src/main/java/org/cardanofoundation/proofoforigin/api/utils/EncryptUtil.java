package org.cardanofoundation.proofoforigin.api.utils;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class EncryptUtil {
    private static final int KEY_SIZE = 256; // Size of the key in bytes
    private static final int SALT_SIZE = 16; // Size of the salt in bytes
    private static final int ITERATION_COUNT = 65536; // Number of iterations for PBKDF2
    private static final String ENCRYPT_ALGORITHM = "AES/ECB/PKCS5Padding";

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        return salt;
    }

    public static SecretKey generateSecretKey(String passwordStr, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        char[] password = passwordStr.toCharArray();
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password, salt, ITERATION_COUNT, KEY_SIZE);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public static byte[] encrypt(byte[] data, String password, byte[] salt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        SecretKey secretKey = generateSecretKey(password, salt);
        return encrypt(data, secretKey);
    }

    public static byte[] decrypt(byte[] encryptedData, String password, byte[] salt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        SecretKey secretKey = generateSecretKey(password, salt);
        return decrypt(encryptedData, secretKey);
    }

    public static byte[] encrypt(byte[] data, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] encryptedData, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedData);
    }
}
