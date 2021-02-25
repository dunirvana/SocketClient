package com.example.socketclient;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Cryptography {

    private static String FAKE_KEY = "FAKE_KEY_ONLY_TO_TEST_1234567890";

    public static String encrypt(String message, String key) throws Exception {

        key = FAKE_KEY;

        byte[] byteMessage = nullPadString(message).getBytes();
        byte[] byteKey = key.getBytes();
        Key secretKey = new SecretKeySpec(byteKey, "AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] cipher = c.doFinal(byteMessage);

        return fromHex(cipher);
    }


    public static String decrypt(String decmessage, String key) throws Exception {

        key = FAKE_KEY;

        byte[] byteKey = key.getBytes();
        Key secretKey = new SecretKeySpec(byteKey,"AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] byteDecoded = c.doFinal(toHex(decmessage));

        return new String(byteDecoded);
    }


    private static String nullPadString(String original) {
        StringBuilder output = new StringBuilder(original);
        int remain = output.length() % 16;
        if (remain != 0) {
            remain = 16 - remain;
            for (int i = 0; i < remain; i++) {
                output.append((char) 0);
            }
        }
        return output.toString();
    }
    public static String fromHex(byte[] hex) {
        StringBuilder sb = new StringBuilder();
        for (byte b : hex) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    public static byte[] toHex(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
