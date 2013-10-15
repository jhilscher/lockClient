package com.tao.lockclient.activities;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tao.lockclient.R;
import com.tao.lockclient.tasks.RestRequestTask;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Click on the scan button.
		final Button scanButton = (Button) findViewById(R.id.scanButton);
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
		
		// click send button
		final Button sendButton = (Button) findViewById(R.id.callRest);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			
				try {
					String response = new RestRequestTask().execute(URL).get();
					Toast.makeText(getApplicationContext(), response, 1).show();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		
		// click register button
		final Button registerButton = (Button) findViewById(R.id.registerButton);
		registerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		
					Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
					MainActivity.this.startActivity(registerIntent);
			}
					
			});			
		
	}
	
	public void handleTaskResponse(String response) {
		Toast.makeText(getApplicationContext(), response, 1).show();
	}
	
	/**
	 * Handles the result from the qr-code-scanner intend.
	 * --> AUTH
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		   if (requestCode == 0) {
		      if (resultCode == RESULT_OK) {
		    	 // QR-Code Content 
		         String contents = intent.getStringExtra("SCAN_RESULT");
		         String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
		         
		         // remove null values from response string 
		         contents = contents.replaceAll("\u0000", "");
		         
		         if(contents.split("#").length != 2) {
		        	 showMessage("Wrong QR-Code!");
		        	 return;
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
		         
		         try {
					String response = new RestRequestTask().execute("https://lockd059130trial.hanatrial.ondemand.com/lock/api/service/auth",
								ID,
								r1).get();
					
					showMessage("Login successful!");
					
					} catch (InterruptedException e) {
						Log.e("InterruptedException", e.getMessage());
					} catch (ExecutionException e) {
						Log.e("ExecutionException", e.getMessage());
					} finally {
						showMessage("Error, while calling server.");
					}
		         
		         
		         
		      } else if (resultCode == RESULT_CANCELED) {
		         // Handle cancel
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
