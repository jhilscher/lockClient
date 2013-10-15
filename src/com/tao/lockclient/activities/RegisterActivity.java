package com.tao.lockclient.activities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tao.lockclient.R;
import com.tao.lockclient.tasks.RestRequestTask;
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

					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
					startActivityForResult(intent, 0);

					} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(getApplicationContext(), "ERROR:" + e, 1).show();

					}
				
			}
		});
		
	
		// TextView for the result
		final TextView resultText = (TextView) findViewById(R.id.resultTextView);
		
		// click on load secret Button
		final Button loadSecretButton = (Button) findViewById(R.id.loadSecretButton);
		loadSecretButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		
					// add result to TextView
					resultText.setText(readFromFile(Util.FILENAME_X1));
				
				}		
			});	
		
	}

	// User internal storage to save seed
	// http://developer.android.com/guide/topics/data/data-storage.html#filesInternal
	// http://android-developers.blogspot.de/2013/02/using-cryptography-to-store-credentials.html
	public boolean saveToFile(String content, String filename) {
		FileOutputStream fos;
		try {
			fos = openFileOutput(filename, Context.MODE_PRIVATE);
			fos.write(content.getBytes());
			fos.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block 
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 
	 * @return null or filecontent as String
	 */
	public String readFromFile(String filename) {
		FileInputStream fis = null;
		// read File
		try {
			fis = openFileInput(filename);
			StringBuffer fileContent = new StringBuffer("");

			byte[] buffer = new byte[1024];

			while (fis.read(buffer) != -1) {
			    fileContent.append(new String(buffer));
			}
			
			fis.close();
			
			// add result to TextView
			return fileContent.toString();

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
		         
		         Util.saveToFile(contents, Util.FILENAME_ID, this);
		         		         
		         try {
		        	String x1 = Util.generateKey(); 

					String response = new RestRequestTask().execute("https://lockd059130trial.hanatrial.ondemand.com/lock/api/service/register",
							contents,
							x1).get();
					
					Util.saveToFile(x1, Util.FILENAME_X1, this);
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		         
		         String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
		         // Handle successful scan
		         
		         Toast.makeText(getApplicationContext(), "Result:" + contents, 1).show();
		         
		      } else if (resultCode == RESULT_CANCELED) {
		         // Handle cancel
		      }
		   }
		}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
		return true;
	}

}
