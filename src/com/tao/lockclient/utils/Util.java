package com.tao.lockclient.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import android.content.Context;
import android.util.Log;

public class Util {

	public static final String FILENAME_ID = "clientidkey";
	public static final String FILENAME_X1 = "x1key.kstore";
	
	public static final String PREFS = "lockPrefs";
	public static final String PREFS_KEY_LOGGEDIN = "loggedIn";
	public static final String PREFS_KEY_REGISTERED = "registered";
	
	public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
	
	public static boolean saveToFile(String content, String filename, Context context) {
		
		FileOutputStream fos;
		try {
			fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
			fos.write(content.getBytes());
			fos.close();
			return true;
		} catch (FileNotFoundException e) {
			Log.e("Error: ", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	
	public static String readFromFile(String filename, Context context) {
		FileInputStream fis = null;
		
		File file = context.getFileStreamPath(filename);
		if(!file.exists()) {
			return null;
		}
		
		// read File
		try {
			fis = context.openFileInput(filename);
			
			InputStreamReader inputStreamReader = new InputStreamReader(fis);
			
		    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
		    
		    StringBuilder sb = new StringBuilder();
		    String line;
		    while ((line = bufferedReader.readLine()) != null) {
		        sb.append(line);
		    }
		    fis.close();
		    
		    return sb.toString();

		} catch (FileNotFoundException e) {
			Log.e("Error: ", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * http://android-developers.blogspot.de/2013/02/using-cryptography-to-store-credentials.html
	 */
	public static String generateKey() throws NoSuchAlgorithmException {

		final int outputKeyLength = 256;
		
	    SecureRandom secureRandom = new SecureRandom();
	    // Do *not* seed secureRandom! Automatically seeded from system entropy.
	    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
	    keyGenerator.init(outputKeyLength, secureRandom);
	    SecretKey key = keyGenerator.generateKey();
	    
	    return toHex(key.getEncoded());
	}
	
    /**
     * https://crackstation.net/hashing-security.htm
     * Converts a byte array into a hexadecimal string.
     *
     * @param   array       the byte array to convert
     * @return              a length*2 character string encoding the byte array
     */
    public static String toHex(byte[] array)
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
    }
    
    /**
     * https://crackstation.net/hashing-security.htm
     * Converts a string of hexadecimal characters into a byte array.
     *
     * @param   hex         the hex string
     * @return              the hex string decoded into a byte array
     */
    public static byte[] fromHex(String hex)
    {
        byte[] binary = new byte[hex.length() / 2];
        for(int i = 0; i < binary.length; i++)
        {
            binary[i] = (byte)Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        }
        return binary;
    }
    
    /**
     * 
     * @param a
     * @param b
     * @return
     */
    public static byte[] xor(byte[] a, byte[] b)
    {
    	int length = Math.min(a.length, b.length);
    	final byte[] output = new byte[length];
    	
        for(int i = 0; i < length; i++)
        	output[i] = (byte) (a[i] ^ b[i]);
        return output;
    }
    
	/**
     * Taken From:
     * https://crackstation.net/hashing-security.htm
     * Computes the PBKDF2 hash of a password.
     *
     * @param   password    the password to hash.
     * @param   salt        the salt
     * @param   iterations  the iteration count (slowness factor)
     * @param   bytes       the length of the hash to compute in bytes
     * @return              the PBDKF2 hash of the password
     */
    public static String pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
        throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return toHex(skf.generateSecret(spec).getEncoded());
    }
}
