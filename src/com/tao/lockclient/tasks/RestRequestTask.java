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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import com.google.gson.GsonBuilder;

import android.os.AsyncTask;
import android.util.Log;

/**
 * 
 * @author Joerg Hilscher.
 * http://stackoverflow.com/questions/3505930/make-an-http-request-with-android
 * for: REGISTRATION!!!
 */
public class RestRequestTask extends AsyncTask<String, String, String>{

	/**
	 * HTTP GET request.
	 * Joerg Hilscher.
	 * args[0]: URL
	 * args[1]: JSON-Value 1
	 * args[2]: JSON-Value 2
	 */
    @Override
    protected String doInBackground(String... args) {
    	
    	// SSL
    	//http://stackoverflow.com/questions/2603691/android-httpclient-and-https
    	
    	// TODO: Alternativer weg: http://developer.android.com/training/articles/security-ssl.html 
    	
    	SchemeRegistry schemeRegistry = new SchemeRegistry();
    	schemeRegistry.register(new Scheme("https", 
    	            SSLSocketFactory.getSocketFactory(), 443));

    	HttpParams params = new BasicHttpParams();

    	SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);

    	HttpClient httpclient = new DefaultHttpClient(mgr, params);
    	

        //HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            
        	// HTTP GET
        	//response = httpclient.execute(new HttpGet(uri[0]));
            
        	if(args.length < 3)
        		throw new IllegalArgumentException("args needs 3 values!");
        	
        	// JSON
        	Map<String, String> comment = new HashMap<String, String>();    
        	comment.put("x1", args[2]);
            comment.put("clientIdKey", args[1]);
            String json = new GsonBuilder().create().toJson(comment, Map.class);

            //passes the results to a string builder/entity
            StringEntity se = new StringEntity(json);
        	
        	// HTTP POST
        	HttpPost post = new HttpPost(args[0]);
        	
        	// TODO: remove
        	Log.i("Json POST: ", json);
        	
        	// set Header
        	post.addHeader("Accept", "application/json");
        	post.addHeader("Content-Type", "application/json");
            post.setEntity(se);
        	
        	response = httpclient.execute(post);
        	
            StatusLine statusLine = response.getStatusLine();
            
            Log.i("Statuscode", "Code: " + statusLine.getStatusCode());
            
            // when 201 created
            if(statusLine.getStatusCode() == HttpStatus.SC_CREATED){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
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
        
        return responseString;
        
    }
    
    

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Do anything with response..
        
    }
}