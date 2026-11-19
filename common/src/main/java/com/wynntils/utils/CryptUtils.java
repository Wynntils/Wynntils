/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils;

import java.security.Key;
import java.security.MessageDigest;
import java.security.PublicKey;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import net.minecraft.util.crypto.CryptException;

// These methods were all from net.minecraft.util.Crypt before it was removed in 26.4
public final class CryptUtils {
    public static SecretKey generateSecretKey() throws CryptException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            throw new CryptException(e);
        }
    }

    public static byte[] digestData(String serverId, PublicKey publicKey, SecretKey sharedKey) throws CryptException {
        try {
            return digestData(serverId.getBytes("ISO_8859_1"), sharedKey.getEncoded(), publicKey.getEncoded());
        } catch (Exception e) {
            throw new CryptException(e);
        }
    }

    private static byte[] digestData(byte[]... inputs) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

        for (byte[] input : inputs) {
            messageDigest.update(input);
        }

        return messageDigest.digest();
    }

    public static byte[] encryptUsingKey(Key key, byte[] input) throws CryptException {
        return cipherData(1, key, input);
    }

    public static byte[] decryptUsingKey(Key key, byte[] input) throws CryptException {
        return cipherData(2, key, input);
    }

    private static byte[] cipherData(int cipherOpMode, Key key, byte[] input) throws CryptException {
        try {
            return setupCipher(cipherOpMode, key.getAlgorithm(), key).doFinal(input);
        } catch (Exception e) {
            throw new CryptException(e);
        }
    }

    private static Cipher setupCipher(int cipherOpMode, String algorithm, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(cipherOpMode, key);
        return cipher;
    }
}
