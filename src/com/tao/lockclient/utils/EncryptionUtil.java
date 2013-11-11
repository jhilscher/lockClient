package com.tao.lockclient.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

/**
 * @author JavaDigest
 * FROM: http://javadigest.wordpress.com/2012/08/26/rsa-encryption-example/
 * 
 * Changes made for android compability. 
 * 
 */
public class EncryptionUtil {

  /**
   * String to hold name of the encryption algorithm.
   */
  public static final String ALGORITHM = "RSA";

  /**
   * String to hold the name of the private key file.
   */
  public static final String PRIVATE_KEY_FILE = "private.key";

  /**
   * String to hold name of the public key file.
   */
  public static final String PUBLIC_KEY_FILE = "public.key";

  public static final int KEY_SIZE = 1024;
  
  /**
   * Generate key which contains a pair of private and public key using 1024
   * bytes. Store the set of keys in Prvate.key and Public.key files.
   * 
   * @throws NoSuchAlgorithmException
   * @throws IOException
   * @throws FileNotFoundException
   */
  public static String generateKey(Context context) {

    KeyPairGenerator keyGen;
	try {
		keyGen = KeyPairGenerator.getInstance(ALGORITHM);

		
		keyGen.initialize(KEY_SIZE);
  		final KeyPair key = keyGen.generateKeyPair();
  		
  		// save private key
		FileOutputStream fos;
		try {
			fos = context.openFileOutput(PRIVATE_KEY_FILE, Context.MODE_PRIVATE);
						
			 // Saving the Private key in a file
		      ObjectOutputStream privateKeyOS = new ObjectOutputStream(fos);
		      privateKeyOS.writeObject(key.getPrivate());
		      privateKeyOS.close();
			
			//fos.write(key.getPrivate().getEncoded());
			fos.close();
		} catch (FileNotFoundException e) {
			Log.e("Error: ", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("Error: ", e.getMessage());
			e.printStackTrace();
		}

		RSAPublicKey rsaPublicKey = (RSAPublicKey)KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(key.getPublic().getEncoded()));
		
		// encode as xml
		
		StringBuilder sb = new StringBuilder();
		
		
		Log.i("Modulus Size: ", "" + rsaPublicKey.getModulus().toByteArray().length );
		Log.i("Modulus Int: ", "" + rsaPublicKey.getModulus() );
		
		// remove first byte
		byte[] modulus = clearFirstByte(rsaPublicKey.getModulus().toByteArray());
		byte[] exponent = clearFirstByte(rsaPublicKey.getPublicExponent().toByteArray());
		
		Log.i("Modulus Size: ", "" + modulus.length );
		
		// encode the key in xml format
		// this makes it readable for .net
		sb.append("<RSAKeyValue>");
		sb.append("<Modulus>" + Base64.encodeToString(modulus, Base64.NO_WRAP) + "</Modulus>");
		sb.append("<Exponent>"  + Base64.encodeToString(exponent, Base64.NO_WRAP) +  "</Exponent>");
		sb.append("</RSAKeyValue>");
		
		
		return sb.toString();
		
		//return (RSAPublicKey)KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key.getPublic().getEncoded())).getEncoded();
		
		//return key.getPublic().getEncoded();
      
	} catch (NoSuchAlgorithmException e1) {
		e1.printStackTrace();
	} catch (InvalidKeySpecException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return null;
  }

  /**
   * Removes first byte from an byte array, if first value is 0.
   * @param input
   * @return
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
   * Encrypt the plain text using public key.
   * 
   * @param text
   *          : original plain text
   * @param key
   *          :The public key
   * @return Encrypted text
   * @throws java.lang.Exception
   */
  public static byte[] encrypt(String text, PublicKey key) {
    byte[] cipherText = null;
    try {
      // get an RSA cipher object and print the provider
      final Cipher cipher = Cipher.getInstance(ALGORITHM);
      // encrypt the plain text using the public key
      cipher.init(Cipher.ENCRYPT_MODE, key);
      cipherText = cipher.doFinal(text.getBytes());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return cipherText;
  }

  /**
   * Decrypt text using private key.
   * 
   * @param text
   *          :encrypted text
   * @param key
   *          :The private key
   * @return plain text
   * @throws java.lang.Exception
   */
  public static String decrypt(byte[] text, Context context) {
    
	PrivateKey key = Util.getKeyFromFile(PRIVATE_KEY_FILE, context);
	
	Log.i("text bytes: ", "length: " + text.length);
	
	byte[] dectyptedText = null;
    try {
      // get an RSA cipher object and print the provider
      // BC will map it to non-padding	
      final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");

      // decrypt the text using the private key
      cipher.init(Cipher.DECRYPT_MODE, key);
      dectyptedText = cipher.doFinal(text);

      //Log.i("decrypted Text ", Util.toHex(dectyptedText));
      
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return Util.toHex(dectyptedText);
  }


}