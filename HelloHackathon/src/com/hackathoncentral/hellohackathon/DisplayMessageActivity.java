package com.hackathoncentral.hellohackathon;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class DisplayMessageActivity extends Activity {

	private static final String ENTRIES = "Entries";
	RemoteServiceImpl pearsonApi = new RemoteServiceImpl();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_message);
		Intent intent = getIntent();
		String text = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		TextView textView = new TextView(this);
		textView.setTextSize(10);
		String apiUrl = "https://api.pearson.com/longman/dictionary/entry.json";
		HashMap<String, String> request = new HashMap<String, String>();
		request.put("q", text);
		request.put("apikey", "079073f61b9f55da0b6401df7c3c9946");
		pearsonApi.setApiUrl(apiUrl);
		pearsonApi.setParams(request);
		String response = "";
		String output = "";
		try {
			response = pearsonApi.response(pearsonApi.connect(request));
		} catch (IOException e) {
			Log.e("Error", "Calling Pearson API:" + e.getMessage());
		}
		try {
			output = parseResponse(response);
			Log.d("Output", output);
			if (!Util.isEmpty(output)) {
				textView.setText(output);
			} else {
				textView.setText("Definition not found. Try another word.");
			}
		} catch (JSONException e) {
			Log.e("Error", "Parsing JSON:" + e.getMessage());
			textView.setText("Definition not found. Try another word.");
			e.printStackTrace();
		}
		setContentView(textView);
	}

	private String parseResponse(String response)
			throws JSONException {
		String output = "";
		if (response.indexOf(ENTRIES) != -1) {
			JSONObject results = new JSONObject(response).optJSONObject(ENTRIES);
			if (results != null && results.length() > 0 && results.has("Entry")) {
				Log.d("Results Length", String.valueOf(results.length()));
				JSONObject word = null;
				JSONArray words = results.optJSONArray("Entry");
				if (words != null) {
					for (int i = 0; i < words.length(); i++) {
						word = (JSONObject) words.get(i);
						if (word.has("Sense"))
							break;
					}
				} else {
					word = (JSONObject) results.get("Entry");
				}
				if (word != null) {
					JSONArray senses = word.optJSONArray("Sense");
					JSONObject sense = null;
					if (senses != null) {
						sense = (JSONObject) senses.get(0);
					} else {
						sense = word.getJSONObject("Sense");
					}

					@SuppressWarnings("rawtypes")
					Iterator keys = sense.keys();
					while (keys.hasNext()) {
						String key = keys.next().toString();
						if ("DEF".equalsIgnoreCase(key)) {
							output += "Definition of Word entered:"
									+ ((JSONObject) sense.getJSONObject("DEF"))
											.getString("#text") + "\n\n\n";
						} else if ("EXAMPLE".equalsIgnoreCase(key)) {
							// output += "\nExample of Word entered:" +
							// ((JSONObject)sense.getJSONObject("EXAMPLE")).getString("#text");
						}
					}
				}
			}
		}
		return output;
	}

}
