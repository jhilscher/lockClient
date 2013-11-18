package com.tao.lockclient.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

/**
 * 
 * @author Joerg Hilscher
 *
 */
public class RSAUtil {


  public static final String ALGORITHM = "RSA";
  public static final String PRIVATE_KEY_FILE = "private.key";
  public static final int KEY_SIZE = 2048; // Bits
  

  /**
   * Generates RSA-Key-Pair.
   * Saves private key.
   * Returns public key.
   * @param context Android Context
   * @return public key, xml-encoded
   */
  public static String generateKey(Context context) {

    KeyPairGenerator keyGen;
	try {
		keyGen = KeyPairGenerator.getInstance(ALGORITHM);

		// generate both keys
		keyGen.initialize(KEY_SIZE);
  		final KeyPair key = keyGen.generateKeyPair();
  		
  		// save private key to internal storage
		FileOutputStream fos;
		
		try {
			
			fos = context.openFileOutput(PRIVATE_KEY_FILE, Context.MODE_PRIVATE);
			ObjectOutputStream privateKeyOS = new ObjectOutputStream(fos);
			privateKeyOS.writeObject(key.getPrivate());
			privateKeyOS.close();
			fos.close();
			
		} catch (FileNotFoundException e) {
			Log.e("Error: ", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			e.printStackTrace();
		}

		// map public key to RSAPublicKey-Object
		RSAPublicKey rsaPublicKey = (RSAPublicKey)KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(key.getPublic().getEncoded()));
		
		// to encode as xml
		StringBuilder sb = new StringBuilder();
		
		// TODO: remove -> should not log this
		Log.i("Modulus Size: ", "" + rsaPublicKey.getModulus().toByteArray().length );
		Log.i("Modulus Int: ", "" + rsaPublicKey.getModulus() );
		
		// remove first byte
		byte[] modulus = clearFirstByte(rsaPublicKey.getModulus().toByteArray());
		byte[] exponent = clearFirstByte(rsaPublicKey.getPublicExponent().toByteArray());
		
		// encode the key in xml format
		// this makes it readable for .net
		// values need to be base64 encoded
		sb.append("<RSAKeyValue>");
		sb.append("<Modulus>" + Base64.encodeToString(modulus, Base64.NO_WRAP) + "</Modulus>");
		sb.append("<Exponent>"  + Base64.encodeToString(exponent, Base64.NO_WRAP) +  "</Exponent>");
		sb.append("</RSAKeyValue>");
		
		// return as String
		return sb.toString();
      
	} catch (NoSuchAlgorithmException e1) {
		e1.printStackTrace();
	} catch (InvalidKeySpecException e) {
		e.printStackTrace();
	}
	return null;
  }

  /**
   * Removes first byte from an byte array, if first value is 0.
   * @param input
   * @return byte array
   */
  private static byte[] clearFirstByte(byte[] input) {
	  if (input.length < 2)
		  return null;
	  
	  // return the input, if first byte is not 0.
	  if (input[0] != 0)
		  return input;
	  
	  byte[] result = new byte[input.length - 1];
	  
	  
	  for (int i = 0; i < result.length; i++) {
		  result[i] = input[i+1];
	  }
	  
	  return result;
  }

  /**
   * Decrypts a RSA-encrypted text. 
   * @param text encrypted text
   * @param context Android Context
   * @return Hex encoded string. 
   */
  public static String decrypt(byte[] cipherText, Context context) {
    
		byte[] plainText = null;
	  
		try {
    	
		  // get the private key from the key file
		  PrivateKey key = Util.getKeyFromFile(PRIVATE_KEY_FILE, context);
	      
	      // BC will map it to non-padding	
	      final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
	
	      // decrypt the text using the private key
	      cipher.init(Cipher.DECRYPT_MODE, key);
	      plainText = cipher.doFinal(cipherText);
	
	    } catch (Exception e) {
	      e.printStackTrace();
	      return null;
	    }

		// return as hex/String
    return Util.toHex(plainText);
  }


}