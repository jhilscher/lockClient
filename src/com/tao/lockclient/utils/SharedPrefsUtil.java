package com.tao.lockclient.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPrefsUtil {

	/**
	 * Inserts a key, value paar into SharedPrefs.
	 * @param key 	String key.
	 * @param value	Boolean value.
	 */
	public static void setSharedPrefs(String key, boolean value, Context context) {
		
		// get SharedPrefs
		SharedPreferences settings = context.getSharedPreferences(Util.PREFS, Context.MODE_PRIVATE);
	   
		Editor editor = settings.edit();
		
	    // put key value
		editor.putBoolean(key, value);
	    
	    // commit
		editor.commit();
	}
	
	/**
	 * get a boolean value from shared prefs.
	 * @param key 	String key.
	 * @return		Boolean value.
	 */
	public static boolean getFromSharedPrefs(String key, Context context) {
		
		// get SharedPrefs
		SharedPreferences settings = context.getSharedPreferences(Util.PREFS, Context.MODE_PRIVATE);
	
		// get and return pref, default is false
		return settings.getBoolean(key, false);
	}
	
	
}
