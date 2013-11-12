package com.tao.lockclient.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tao.lockclient.R;
import com.tao.lockclient.tasks.RestRequestTask;
import com.tao.lockclient.utils.RSAUtil;
import com.tao.lockclient.utils.Util;

public class RegisterActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
				
		// Click on the scan button  to register.
		final Button scanButton = (Button) findViewById(R.id.scanToRegisterButton);
		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				try {
						// call scanner
						Intent intent = new Intent("com.google.zxing.client.android.SCAN");
						intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
						startActivityForResult(intent, 0);
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(getApplicationContext(), "ERROR:" + e, 1).show();
					}
				
			}
		});
		
	}

	
	
	/**
	 * Handles the result from the qr-code-scanner intend.
	 * --> REGISTER
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		   if (requestCode == 0) {
		      if (resultCode == RESULT_OK) {
		    	 
		    	  // QR-Code Content 
		         String contents = intent.getStringExtra("SCAN_RESULT");
		         
		         // remove null values from response string 
		         contents = contents.replaceAll("\u0000", "");

		         // decrypted text as hex-string
		         String x1 = RSAUtil.generateKey(this);
		         
		         // save to file
		         Util.saveToFile(contents, Util.FILENAME_ID, this);

		         AsyncTask<String, String, Boolean> task = new RestRequestTask(this, 
	        			RegisterActivity.this, 
	        			"Trying to register ...",
	        			"Registration successfull",
	        			"Registration failed.",
	        			RestRequestTask.TaskType.REGISTER);
	        	
		         task.execute("https://lockd059130trial.hanatrial.ondemand.com/lock/api/service/register",
						contents,
						x1);

		         String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
		         		         
		      } else if (resultCode == RESULT_CANCELED) {
		         // Handle cancel
		      }
		   }
		}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

}
