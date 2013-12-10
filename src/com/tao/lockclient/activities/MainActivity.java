package com.tao.lockclient.activities;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureActivity;
import com.tao.lockclient.R;
import com.tao.lockclient.tasks.RestRequestTask;
import com.tao.lockclient.utils.RSAUtil;
import com.tao.lockclient.utils.SharedPrefsUtil;
import com.tao.lockclient.utils.Util;

/**
 * 
 * @author Joerg Hilscher
 *
 */
public class MainActivity extends Activity { 

	/**
	 * URL to the cloud-api method.
	 */
	private final String URL = "https://lockd059130trial.hanatrial.ondemand.com/lock/api/service/auth";
	
	private Button scanButton;
	
	private Button registerButton;
	
	private String scanContents;
	
	private CheckBox checkBoxRegistered;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final Context context = this;
		
		// Scan button for login
		scanButton = (Button) findViewById(R.id.scanButton);
		
		// OnClickListener
		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
						//Intent intent = new Intent("com.google.zxing.client.android.SCAN");
						Intent intent = new Intent(context, CaptureActivity.class);
						intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
						startActivityForResult(intent, 0);
					} catch (Exception e) {
						Log.e("Error opening QR-Code-Scanner", e.getMessage());
						showMessage(e.getMessage());
					}
			}
		});
		
//		// register button
//		registerButton = (Button) findViewById(R.id.registerButton);
//		
//		// OnClickListener
//		registerButton.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//						Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
//						MainActivity.this.startActivity(registerIntent);
//					}	
//			});			

		
		// checkbox
		checkBoxRegistered = (CheckBox) findViewById(R.id.checkBoxRegistered);
		
		checkIfRegistered();
		
	}
	
	// resuming
	@Override
	public void onResume() {
	    super.onResume();  // Always call the superclass method first

	    // check
	    checkIfRegistered();
	    
	}
	
	/**
	 * Checks if the app is registered and sets info.
	 */
	private Boolean checkIfRegistered() {
		boolean appIsRegistered = SharedPrefsUtil.getFromSharedPrefs(Util.PREFS_KEY_REGISTERED, this);
		
		// if registered, set color of button to green
		if(appIsRegistered) {
			checkBoxRegistered.setTextColor(getResources().getColor(R.color.green));
			checkBoxRegistered.setChecked(true);
			checkBoxRegistered.setText(getResources().getString(R.string.checkBoxRegistered));
			scanButton.setEnabled(true);
			return true;
		} else {
			checkBoxRegistered.setTextColor(getResources().getColor(R.color.red));
			checkBoxRegistered.setChecked(false);
			checkBoxRegistered.setText(getResources().getString(R.string.checkBoxNotRegistered));
			scanButton.setEnabled(false);
			return false;
		}
		
	}
	
	/**
	 * 
	 * @return Boolean if successful.
	 */
	public Boolean loginToServer() {

		    	  // QR-Code Content 
		         String contents = this.scanContents;
		         
		         // reset
		         this.scanContents = null;
		         
		         if (contents == null)
		        	 return false;
		         
		         
		         // remove null values from response string 
		         contents = contents.replaceAll("\u0000", "");
		         contents = contents.replaceAll("\"", "");
		         
		         Log.i("contents: ", contents);
		         
		         
		         byte[] plainTextBytes;
		         
		         try {
			         // decrypt, to get one time token r1
			        plainTextBytes = RSAUtil.decrypt(Util.fromHex(contents), this);
		         
		         } catch (Exception ex) {
		        	 showMessage("Wrong Key!");
		        	 return false;
		         }
		         
		         if(plainTextBytes == null || plainTextBytes.length < 120) {
		        	 showMessage("Wrong QR-Code!");
		        	 return false;
		         }
		         
		         // token, use hex-converter on this part of the plainTextBytes
		         String token = Util.toHex(Arrays.copyOfRange(plainTextBytes, 0, plainTextBytes.length - 13));
		         
		         // to Unicode for timestamp
		         String timestamp = new String(Arrays.copyOfRange(plainTextBytes, plainTextBytes.length - 13, plainTextBytes.length)); 

		         // get ID from file
		         String ID = Util.readFromFile(Util.FILENAME_ID, this);
		         
		         Log.i("timestamp: ", timestamp);
		         
		         Log.i("time: ", "" + new Date().getTime());
		         
		         Log.i("token: ", token);
		         
		         // verify timestamp
		         try {
		        	 if (Math.abs(new Date().getTime() - Long.parseLong(timestamp)) > 60*2*1000)   {
		        		 showMessage("QR-Code is too old.");
		        		 return false; // cancel if timedifference is over 2 mins
		        	 }
		        	 
		         } catch (Exception e) {
		        	 showMessage("QR-Code not valid!");
		        	 return false;
		         }

		         
		         byte[] ts = timestamp.getBytes();
		         
		         Log.i("timestampbytes: l " + ts.length,  "");
		         
		        try {
					
		        	// hash the result, with the timestamp as salt
		        	String r1 =  Util.pbkdf2(token.toUpperCase().toCharArray(), timestamp.getBytes(), 1000, 64);
				
		        	Log.i("r1: ", r1);
		        	Log.i("timestamp: ", timestamp);
		         
			        AsyncTask<String, String, Boolean> task = new RestRequestTask(
			        		this, 
			        		MainActivity.this,
			        		"Trying to log in ...",
			        		"Log in successful.",
			        		"Failed to log in.",
			        		RestRequestTask.TaskType.LOGIN)
			        .execute(URL, ID, r1);

		        } catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
					return false;
				} catch (InvalidKeySpecException e) {
					e.printStackTrace();
					return false;
				}
		        
		        return true;		   
		}

	
	/**
	 * Handles the result from the qr-code-scanner intend.
	 * --> AUTH
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		   if (requestCode == 0) {
		      if (resultCode == RESULT_OK) {
		    	 
		    	  // QR-Code Content 
		         this.scanContents = intent.getStringExtra("SCAN_RESULT");
		         
		         // not needed
		         // String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
		         
		         loginToServer();

		      } else if (resultCode == RESULT_CANCELED) {
		    	  showMessage("Scan NOT successful!");
		      }
		   }
	}

	/**
	 * Displays a short message as Toast.
	 * @param msg String of the message.
	 */
	public void showMessage(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * On Click Menu Item
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

	    switch (item.getItemId()) { 
	        case R.id.action_settings: // Settings
	        	Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
				MainActivity.this.startActivity(registerIntent);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
