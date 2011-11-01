/*
 * Copyright (C) 2010 Jean-Pierre Cadiat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.homelinux.penecoptero.android.citybikes.app;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccountHelper {

	public static final String SERVER_URL = "https://gw.cyclocity.fr/3311a6cea2e49b10";
	
	private static RESTHelper mRESTHelper;				// Handles http requests
	private String token = null; 						// Stores identification token
	private Date expiryDate = new Date(Long.MAX_VALUE);	// The token have a expiry date
	
	
	/**
	 * Return a map with subscriber account information
	 * @param city The city of the subscription
	 * @param subscriberNumber the ID of the subscriber as indicated on his card
	 * @param subscriberName The family name of the subscriber
	 * @param subscriberPIN The PIN code of the subscriber
	 * @return A map with subscriber account information
	 */
	public JSONObject getSubscriberAccountInfo(
			String city, String subscriberNumber, String subscriberName, String subscriberPIN){
		String info = getUrl(SERVER_URL + "/client/" + city + "/info/" + subscriberNumber + "?cltNm=" +
                subscriberName + "&cltPin=" + subscriberPIN);
		try {
			return new JSONObject(info);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new JSONObject();
	}

	private String getUrl(String url) {
		// Check if token is still valid
		if (token == null || expiryDate.after(new Date(System.currentTimeMillis()))){
			try {
				// Get token
				JSONObject response = new JSONObject(
					getRESTHelper().restGET(
							SERVER_URL + "/token/key/b885ab926fdca7dbfbf717084fb36b5f"));
				// Token ID
				token = response.getString("token");
				// Get expiration date given as UTC date and convert it to local time zone
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
				expiryDate = sdf.parse(response.getString("endValidity"));
				expiryDate.setTime(expiryDate.getTime() + 
						TimeZone.getDefault().getOffset(expiryDate.getTime()));
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (HttpException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			return getRESTHelper().restGET(url + (url.indexOf('?') > 0 ? '&' : '?') + "token=" + token);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * @return Returns a List of cities. Each city is described in a Map of Strings.
	 * Each Map contains a "code" value for the city code and a "name" value containing
	 * the name of the city.
	 */
	public List<Map<String, String>> getCities(){
		try {
			String citiesString = getUrl(SERVER_URL + "/contracts/full");
			JSONArray cities = new JSONArray(citiesString);
			ArrayList<Map<String, String>> citiesList = new ArrayList<Map<String, String>>();
			for (int i=0 ; i < cities.length() ; i++){
				JSONObject city = cities.getJSONObject(i);
				Map<String, String> cityMap = new HashMap<String, String>(2);
				cityMap.put("code", city.getString("code"));
				cityMap.put("name", city.getString("name"));
				citiesList.add(cityMap);
			}
			return citiesList;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList<Map<String, String>>();
		}
	}
	
	private RESTHelper getRESTHelper(){
		if (mRESTHelper == null){
			mRESTHelper = new RESTHelper(false, null, null);
		}
		return mRESTHelper;
	}
}
