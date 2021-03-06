package com.tao.lockclient.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Utility Methods. 
 * @author Joerg Hilscher
 *
 */
public class Util {

	public static final String FILENAME_ID = "clientidkey";
	public static final String FILENAME_X1 = "x1key.kstore";
	
	public static final String PREFS = "lockPrefs";
	public static final String PREFS_KEY_LOGGEDIN = "loggedIn";
	public static final String PREFS_KEY_REGISTERED = "registered";
	
	public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";
	
	/**
	 * 
	 * @param filename
	 * @param context
	 * @return
	 */
	public static boolean deleteKey(Context context) {
		
		File fileKey = new File(FILENAME_X1);
		File fileID = new File(FILENAME_ID);
		
		
		// delete a key
		KeyStore ks = KeyStore.getInstance();
		boolean success = ks.delete("secretKey1");
		
		return fileKey.delete() && fileID.delete();
		
	}
	
	/**
	 * Saves a file to internal storage.
	 * @param content	Content of the file as string.
	 * @param filename	Filename.
	 * @param context	Android Context.
	 * @return	Boolean if successful.
	 */
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
		
     /**
      * Gets a stored private key.
      * @param filename	filename of the key.
      * @param context	Android context.
      * @return	The key.
      */
     public static PrivateKey getKeyFromFile(String filename, Context context) {
 		FileInputStream fis = null;
 		
 		File file = context.getFileStreamPath(filename);
 		
 		// exit, if no key found
 		if(!file.exists()) {
 			return null;
 		}
 		
 		// read File
 		try {
 			fis = context.openFileInput(filename);
 			
 			ObjectInputStream inputStream = null;

 		    inputStream = new ObjectInputStream(fis);
 		    final PrivateKey privateKey = (PrivateKey) inputStream.readObject();
 			
 		    fis.close();
 		    
 		    return privateKey;

 		} catch (FileNotFoundException e) {
 			Log.e("Error: ", e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			Log.e("Error: ", e.getMessage());
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			Log.e("Error: ", e.getMessage());
			e.printStackTrace();
		}
 		return null;
 	} 
    
     /**
      * Get the content of a file.
      * @param filename	filename.
      * @param context	Android context.
      * @return	Content of the file as string.
      */
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
	 * 
	 * After recommendations from android-developers
	 * http://android-developers.blogspot.de/2013/02/using-cryptography-to-store-credentials.html
	 */
	@Deprecated
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
     * XOR 2 byte arrays.
     * Length will be the length if the shorter array.
     * @param a
     * @param b
     * @return	xored byte array.
     */
    @Deprecated
    public static byte[] xor(byte[] a, byte[] b)
    {
    	int length = Math.min(a.length, b.length);
    	final byte[] output = new byte[length];
    	
        for(int i = 0; i < length; i++)
        	output[i] = (byte) (a[i] ^ b[i]);
        return output;
    }
    
	/**
	 * Hash-Algorithm: pbkdf2
	 * 
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
    
    /**
     * Check if an internet connection exists
     * 
     * @param context
     * @return Bool
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}
