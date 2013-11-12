	package com.tao.lockclient.tasks;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.tao.lockclient.utils.SharedPrefsUtil;
import com.tao.lockclient.utils.Util;

/**
 * 
 * @author Joerg Hilscher.
 * http://stackoverflow.com/questions/3505930/make-an-http-request-with-android
 * for: REGISTRATION and AUTHENTIFICATION.
 */
public class RestRequestTask extends AsyncTask<String, String, Boolean>{

	private Context context;
	
	private ProgressDialog pdia;
	
	private String loadMsg;
	
	private String successMsg;
	
	private String failMsg;
	
	public TaskType type;
	
	private final int SSL_PORT = 443;
	
	private final String JSON_X1 = "x1";
	private final String JSON_IDKEY = "clientIdKey";
	
	public enum TaskType {
		REGISTER, LOGIN
	}
	
	/**
	 * Constructor.
	 * @param context		Android Context
	 * @param activity		Launching Activity
	 * @param loadMsg		Message while loading
	 * @param successMsg	Message after success
	 * @param failMsg		Message after failure
	 * @param type			ENUM: Type of request: Login or Register
	 */
	public RestRequestTask(Context context, Activity activity, String loadMsg, String successMsg, String failMsg, TaskType type) {

		this.context = context;
		this.pdia = new ProgressDialog(activity);
		this.loadMsg = loadMsg;
		this.successMsg = successMsg;
		this.failMsg = failMsg;
		this.type = type;
	}
	
	/**
	 * HTTPS POST request.
	 * @author Joerg Hilscher.
	 * args[0]: URL
	 * args[1]: JSON-Value 1
	 * args[2]: JSON-Value 2
	 * 
	 * @return Boolean if successful
	 */
    @Override
    protected Boolean doInBackground(String... args) {
    	
    	// SSL    	
    	Log.i("Task execution: ", "started");
    	
    	Boolean result = false;
    	
    	SchemeRegistry schemeRegistry = new SchemeRegistry();
    	schemeRegistry.register(new Scheme("https", 
    	            SSLSocketFactory.getSocketFactory(), SSL_PORT));

    	HttpParams params = new BasicHttpParams();

    	SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);

    	HttpClient httpclient = new DefaultHttpClient(mgr, params);
    	
        HttpResponse response;
        String responseString = null;
        
        try {
                        
        	if(args.length < 3)
        		throw new IllegalArgumentException("args needs 3 values!");
        	
        	// JSON
        	Map<String, String> comment = new HashMap<String, String>();    
        	comment.put(JSON_X1, args[2]);
            comment.put(JSON_IDKEY, args[1]);
            String json = new GsonBuilder().create().toJson(comment, Map.class);

            //passes the results to a string builder/entity
            StringEntity se = new StringEntity(json);
        	
        	// HTTP POST
        	HttpPost post = new HttpPost(args[0]);
        	
        	// TODO: remove
        	//Log.i("Json POST: ", json);
        	
        	// set Header
        	post.addHeader("Accept", "application/json");
        	post.addHeader("Content-Type", "application/json");
            post.setEntity(se);
        	
            // get response
        	response = httpclient.execute(post);
        	
            StatusLine statusLine = response.getStatusLine();
            
            Log.i("Statuscode", "Code: " + statusLine.getStatusCode());
            
            // when 201 created OR 200 ok
            if(statusLine.getStatusCode() == HttpStatus.SC_CREATED
            		|| statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
                
                result = true;
                
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IllegalStateException("Statuscode not expected: " + statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
        	Log.e("ClientProtocolException", e.getMessage());
        } catch (IOException e) {
        	Log.e("IOException", e.getMessage());
        } catch (IllegalStateException e) {
        	Log.e("IOException", e.getMessage());
        }
        
        Log.i("Response: ", "string: " + responseString);
        
        return result;
        
    }
    
    @Override
    protected void onPreExecute() {
    	super.onPreExecute();
    	
    	Log.i("RequestTask", "onPreExecute fired");
    	
    	if (context == null)
    		return;
    	
    	// set Dialog while working.
        pdia.setMessage(loadMsg);
        pdia.show();   
    }


    /**
     * Method executed after doInBackgound.
     * @param result	if doInBackground was successful.
     */
    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        
        Log.i("RequestTask", "onPostExecute fired");
        
        if (pdia == null)
        	return;

        // remove Loading-Dialog.
        pdia.dismiss();
        
        if (result) {
        
	        if (type == TaskType.LOGIN)
	        	SharedPrefsUtil.setSharedPrefs(Util.PREFS_KEY_LOGGEDIN, true, context);
	        else if (type == TaskType.REGISTER)
	        	SharedPrefsUtil.setSharedPrefs(Util.PREFS_KEY_REGISTERED, true, context);
	        
	        Toast.makeText(context, this.successMsg, Toast.LENGTH_SHORT).show();
        
        } else {
        	
	        if (type == TaskType.LOGIN)
	        	SharedPrefsUtil.setSharedPrefs(Util.PREFS_KEY_LOGGEDIN, false, context);
	        else if (type == TaskType.REGISTER)
	        	SharedPrefsUtil.setSharedPrefs(Util.PREFS_KEY_REGISTERED, false, context);
        	
        	Toast.makeText(context, this.failMsg, Toast.LENGTH_SHORT).show();
        	
        }

    }
}