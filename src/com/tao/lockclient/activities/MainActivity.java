package com.tao.lockclient.activities;

import java.net.ResponseCache;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tao.lockclient.R;
import com.tao.lockclient.tasks.RestRequestTask;
import com.tao.lockclient.utils.Util;
import com.tao.lockclient.utils.SharedPrefsUtil;

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
	
	private RestRequestTask requestTask;
	
	private String scanContents;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		boolean appIsRegistered = SharedPrefsUtil.getFromSharedPrefs(Util.PREFS_KEY_REGISTERED, this);
		boolean appIsLoggedIn = SharedPrefsUtil.getFromSharedPrefs(Util.PREFS_KEY_LOGGEDIN, this);
		
		// Click on the scan button.
		scanButton = (Button) findViewById(R.id.scanButton);
		
		// if registered, set color of button to green
		if(appIsLoggedIn) {
			scanButton.getBackground().setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.MULTIPLY);
		}
		
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
		
		// click register button
		registerButton = (Button) findViewById(R.id.registerButton);
		
		// if registered, set color of button to green
		if(appIsRegistered) {
			registerButton.setBackgroundColor(getResources().getColor(R.color.green));
		}
		
		// OnClickListener
		registerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
						Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
						MainActivity.this.startActivity(registerIntent);
					}	
			});			

	}
	
	// resuming
	@Override
	public void onResume() {
	    super.onResume();  // Always call the superclass method first

		boolean appIsRegistered = SharedPrefsUtil.getFromSharedPrefs(Util.PREFS_KEY_REGISTERED, this);
		boolean appIsLoggedIn = SharedPrefsUtil.getFromSharedPrefs(Util.PREFS_KEY_LOGGEDIN, this);

		Log.i("id registered? : ", "Bool: " + appIsRegistered);
		Log.i("id loggedIn? : ", "Bool: " + appIsLoggedIn);
		
	    if(appIsRegistered) {
	    	registerButton.setBackgroundColor(getResources().getColor(R.color.green));
	    	registerButton.setText("App is registered!");
		}
	    
		if(appIsLoggedIn) {
			scanButton.setBackgroundColor(getResources().getColor(R.color.green));
			scanButton.setText("You have logged in");
		}
		
		if (this.scanContents != null) {
			loginToServer();
		}
	}
	
	
	/**
	 * Handles the result from the qr-code-scanner intend.
	 * --> AUTH
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
		         
		         if(contents.split("#").length != 2) {
		        	 showMessage("Wrong QR-Code!");
		        	 return false;
		         }
		        	 
		         
		         String alpha = contents.split("#")[0];
		         String timestamp = contents.split("#")[1];
		         
		         String x1 = Util.readFromFile(Util.FILENAME_X1, this);
		         String ID = Util.readFromFile(Util.FILENAME_ID, this);
		         
		         Log.i("x1: ", x1);
		         Log.i("alpha: ", alpha);
		         
		         // decrypt, to get one time token r1
		         String r1 = Util.toHex(Util.xor(Util.fromHex(x1), Util.fromHex(alpha)));
		         x1 = null;
		         
		         //TODO: hash r1 with timestamp
		         
		        AsyncTask<String, String, Boolean> task = new RestRequestTask(
		        		this, 
		        		MainActivity.this,
		        		"Trying to log in ...",
		        		"Log in successful.",
		        		"Failed to log in.",
		        		RestRequestTask.TaskType.LOGIN)
		        .execute("https://lockd059130trial.hanatrial.ondemand.com/lock/api/service/auth",
								ID,
								r1);

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
