package com.hackathoncentral.hellohackathon;

import static com.hackathoncentral.hellohackathon.Util.isEmpty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class RemoteServiceImpl {
	
	private String apiUrl;
	
	private Map<String,String> params;

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getApiUrl() {
		return apiUrl;
	}
	
	public void setParams(Map<String,String> params) {
		this.params = params;
	}

	public Map<String,String> getParams() {
		return params;
	}
	
	public URLConnection connect(HashMap<String,String> req) {
		
		try {
			URL url = new URL(apiUrl + path("",req,getParams()));

			Log.d("RemoteService","connect() " + url);

			URLConnection cxn = (URLConnection) url.openConnection();
			cxn.setRequestProperty("Accept-Charset", "UTF-8");
			return cxn;
		} catch (Exception ex) {
			Log.e("RemoteService", "failure building connection. path[ " + apiUrl + " ]" + ex.getMessage());
		}
		return null;
	}
	
	
	public String response(URLConnection cxn) throws IOException {
		BufferedReader r = new BufferedReader(
				new InputStreamReader(cxn.getInputStream()));
		String output = "";
		try {
			String l;
			while ((l = r.readLine()) != null) {
				output += l;
			}
			Log.i("RemoteService","response(): " + output);
		} finally {
	        if (r != null) r.close(); 
	    }
		return output;
	}
	
	public String path(HashMap<String,String> req) {
		return path("", req, null);
	}
	
	public String path(String base){
		return path(base, null, null);
	}
	
	public String path(String base, HashMap<String,String> req, Map<String,String> args) {
		
		Log.d("RemoteService",base);
		
		String path = base + (base.contains("?") ? "" : "?");
		
		if(params != null){
			for (String key: params.keySet()) {
				
				String value = findParamValue(key, req, args);
				
				if (!isEmpty(value)) {
					try {
						path += (path.endsWith("?") ? "" : "&");
						path += key + "=" + (URLEncoder.encode(value, "UTF-8"));
						Log.d("RemoteService","path.param: " + key + " " + value);
					} catch (UnsupportedEncodingException e) {
						Log.e("RemoteService","apparently UFT-8 is not a valid encoding...");
					} 
				} else {
					path = "";
				}
			}
		}
		Log.d("RemoteService","path() result: " + path);
		return path;
	}
	
	protected String findParamValue(String key, Map<String,String> req, Map<String,String> args) {
		String value = "";
		if(args != null && args.containsKey(key)){
			value = args.get(key);
		}
		if(isEmpty(value) && params.containsKey(key)){
			value = params.get(key);	
		}
		if(isEmpty(value) && req != null){
			value = (String) req.get(key);
		}
		if(isEmpty(value)){
			value = "";
		}
		return value;
	}
	
}