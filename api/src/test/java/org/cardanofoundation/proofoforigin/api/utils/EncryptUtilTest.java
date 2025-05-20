package org.cardanofoundation.proofoforigin.api.utils;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EncryptUtilTest {

    @Test
    public void testGenerateSalt() {
        byte[] salt = EncryptUtil.generateSalt();
        assertNotNull(salt);
        assertEquals(16, salt.length);
    }

    @Test
    public void testGenerateSecretKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String password = "password";
        byte[] salt = EncryptUtil.generateSalt();
        SecretKey secretKey = EncryptUtil.generateSecretKey(password, salt);
        assertNotNull(secretKey);
        assertEquals("AES", secretKey.getAlgorithm());
    }

    @Test
    public void testEncryptAndDecrypt() throws Exception {
        String password = "password";
        byte[] salt = EncryptUtil.generateSalt();
        byte[] originalData = "Hello, World!".getBytes();

        byte[] encryptedData = EncryptUtil.encrypt(originalData, password, salt);
        assertNotNull(encryptedData);

        byte[] decryptedData = EncryptUtil.decrypt(encryptedData, password, salt);
        assertNotNull(decryptedData);
        assertArrayEquals(originalData, decryptedData);
    }

    @Test
    public void testEncryptAndDecryptWithSecretKey() throws Exception {
        byte[] keyBytes = new byte[32]; // AES 256-bit key
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        byte[] originalData = "Hello, World!".getBytes();

        byte[] encryptedData = EncryptUtil.encrypt(originalData, secretKey);
        assertNotNull(encryptedData);

        byte[] decryptedData = EncryptUtil.decrypt(encryptedData, secretKey);
        assertNotNull(decryptedData);
        assertArrayEquals(originalData, decryptedData);
    }
}