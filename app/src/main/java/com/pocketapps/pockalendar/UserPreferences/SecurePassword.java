package com.pocketapps.pockalendar.UserPreferences;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by chandrima on 11/03/18.
 */

public class SecurePassword {
    private String salt = "";
    private String hash = "";
    private SecureRandom secureRandom;
    private MessageDigest messageDigest;
    private byte[] buffer;
    private byte[] digest;

    public String getSalt() {
        return this.salt;
    }

    private void setSalt() {
        byte[] salt = new byte[20];
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.nextBytes(salt);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        this.salt = Base64.encodeToString(salt,Base64.DEFAULT);
    }

    public String getSaltedDigest(String password) {
        setSalt();
        return getSalt()+ getDigest(password);
    }

    public String getDigest(String password) {
        try {
            messageDigest = MessageDigest.getInstance("SHA256");
            makeHash(password);
            // hash twice for better securiry
            makeHash(hash);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hash;
    }

    private void makeHash(String data) {
        messageDigest.reset();
        buffer = data.getBytes();
        messageDigest.update(buffer);
        digest = messageDigest.digest();
        hash = "";
        for (int i = 0; i < digest.length; i++) {
            // base or radix 16 for hexadecimal number system - often used in computing as a compacter representation of binary
            // (1 hex digit per 4 bits). The 16 digits are "0–9" followed by "A–F" or "a–f".
            hash +=  Integer.toString((digest[i] & 0xff), 16);}
    }
}
