package com.tao.lockclient.activities;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

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
		
		// Scan button for login
		scanButton = (Button) findViewById(R.id.scanButton);
		
		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
						Intent intent = new Intent("com.google.zxing.client.android.SCAN");
						intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
						startActivityForResult(intent, 0);
					} catch (Exception e) {
						Log.e("Error opening QR-Code-Scanner", e.getMessage());
						showMessage(e.getMessage());
					}
			}
		});
		
		// register button
		registerButton = (Button) findViewById(R.id.registerButton);
		
		// OnClickListener
		registerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
						Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
						MainActivity.this.startActivity(registerIntent);
					}	
			});			

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
	private void checkIfRegistered() {
		boolean appIsRegistered = SharedPrefsUtil.getFromSharedPrefs(Util.PREFS_KEY_REGISTERED, this);
		
		// if registered, set color of button to green
		if(appIsRegistered) {
			checkBoxRegistered.setTextColor(getResources().getColor(R.color.green));
			checkBoxRegistered.setChecked(true);
			checkBoxRegistered.setText(getResources().getString(R.string.checkBoxRegistered));
		} else {
			checkBoxRegistered.setTextColor(getResources().getColor(R.color.red));
			checkBoxRegistered.setChecked(false);
			checkBoxRegistered.setText(getResources().getString(R.string.checkBoxNotRegistered));
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
		         
		         if(contents.split("#").length != 2) {
		        	 showMessage("Wrong QR-Code!");
		        	 return false;
		         }
		        	 
		         
		         String alpha = contents.split("#")[0];
		         String timestamp = contents.split("#")[1];
		         
		         //String x1 = Util.readFromFile(Util.FILENAME_X1, this);
		         String ID = Util.readFromFile(Util.FILENAME_ID, this);
		         
		         //Log.i("x1: ", x1);
		         Log.i("alpha: ", alpha);
		         Log.i("alpha-length: ", "" + alpha.length());
		         
		         // decrypt, to get one time token r1
		         String t1 = RSAUtil.decrypt(Util.fromHex(alpha), this);
		         		         
		         Log.i("t1: ", t1);
		         
		         byte[] ts = timestamp.getBytes();
		         
		         Log.i("timestampbytes: l " + ts.length,  "");
		         
		        try {
					
		        	// hash the result, with the timestamp as salt
		        	String r1 =  Util.pbkdf2(t1.toUpperCase().toCharArray(), timestamp.getBytes(), 1000, 64);
				
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
		         String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
		         
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
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
