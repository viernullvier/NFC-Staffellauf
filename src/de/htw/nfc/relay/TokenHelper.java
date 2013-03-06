package de.htw.nfc.relay;

import java.util.Random;

public class TokenHelper {
    
    private static TokenHelper instance;
    
    private Random randomGenerator;
    
    //private static byte[] keys = new byte[] {7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47};

    public static final int TOKEN_A = 2;
    public static final int TOKEN_B = 1;
    
    private TokenHelper() {
        if (null != TokenHelper.instance) {
            throw new IllegalStateException();
        }
        this.randomGenerator = new Random();
    }
    
    public static TokenHelper getInstance() {
        if (null == TokenHelper.instance) {
            TokenHelper.instance = new TokenHelper();
        }
        return TokenHelper.instance;
    }
    
    public byte[] makeToken(int num) {
        if (num < 0) num = 0;
        int length = num * (TokenHelper.TOKEN_A + TokenHelper.TOKEN_B);
        byte[] result = new byte[length];
        this.randomGenerator.nextBytes(result);
        return result;
    }
    
    public byte[] encryptToken (byte[] input, int key, int num) {
        if (null == input) return null;
        if (key < 0) key = -key;
        byte[] output = input.clone();
        int start = TOKEN_A * num + TOKEN_B * (num - 1);
        if (start < 0) start = 0;
        int end = (TOKEN_A + TOKEN_B) * (num + 1);
        if (end > input.length) end = input.length;
        byte[] lookupTable = createLookupTable(key);
        for (int i = start; i < end; i++) {
            output[i] = lookupTable[input[i] & 0xff];
        }
        
        return output;
    }
    
    private byte[] createLookupTable(int key) {
        if (key < 0) key = -key;
        byte[] output = new byte[256];
        byte[] used = new byte[256];
        int current = key % 256;
        for (int i = 0; i <= 255; i++) {
            while (used[current] != 0) {
                current += key;
                current %= 256;
                while (used[current] != 0) {
                    current++;
                    current %= 256;
                }
            }
            output[current] = (byte) (i - 128);
            used[current] = 1;
        }
        return output;
    }
    
    public static String hexString(byte[] b) {
        if (null == b) return "";
        return hexString(b, 0, b.length);
    }
     
    public static String hexString(byte[] b, int start, int length) {
        if (null == b) return "";
        String result = "";
        for (int i=start; i < start + length; i++) {
            result += Integer.toString(( b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
    
}
