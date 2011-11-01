package net.homelinux.penecoptero.android.citybikes.app;

import java.util.Map;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

public class AccountSettingsActivity extends Activity {
	
	Spinner mSubscriberCity;
	EditText mSubscriberId;
	EditText mSubscriberName;
	EditText mSubscriberPin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_settings);
		
		mSubscriberCity = (Spinner) findViewById(R.id.city);
		mSubscriberId = (EditText) findViewById(R.id.subscriber_id);
		mSubscriberName = (EditText) findViewById(R.id.subscriber_name);
		mSubscriberPin = (EditText) findViewById(R.id.subscriber_pin);
		
		fillData();
	}

	@SuppressWarnings("unchecked")
	private void fillData() {
		AccountHelper helper = new AccountHelper();
		SharedPreferences settings = getSharedPreferences(CityBikes.PREFERENCES_NAME,0);
		
		// Load existing values
		String city = settings.getString("city", null);
		String subscriberNumber = settings.getString("subscriberNumber", null);
		String subscriberName = settings.getString("subscriberName", null);
		String subscriberPIN = settings.getString("subscriberPIN", null);

		// Load complete list of cities and show them in a spinner
		final String[] from = new String[] {"name", "code"};
		final int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

		SimpleAdapter simpleAdapter = new SimpleAdapter(this, helper.getCities(), 
				android.R.layout.simple_spinner_item, from, to);
		simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSubscriberCity.setAdapter(simpleAdapter);
		
		if (city != null){
			// TODO find right id to select
			for (int i=0 ; i < simpleAdapter.getCount() ; i++){
				Map<String, String> cityMap = (Map<String, String>)simpleAdapter.getItem(i);
				if (cityMap.get("code").equals(city)){
					mSubscriberCity.setSelection(i);
					break;
				}
			}
		}
		
		mSubscriberId.setText(subscriberNumber);
		mSubscriberName.setText(subscriberName);
		mSubscriberPin.setText(subscriberPIN);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onPause() {
		SharedPreferences settings = getSharedPreferences(CityBikes.PREFERENCES_NAME,0);
		
		Editor editor = settings.edit();
		Map<String, String> item = (Map<String, String>)mSubscriberCity.getSelectedItem();
		editor.putString("city", item.get("code"));
		
		editor.putString("subscriberNumber", 
				mSubscriberId.getText().toString());
		editor.putString("subscriberName", 
				mSubscriberName.getText().toString());
		editor.putString("subscriberPIN", 
				mSubscriberPin.getText().toString());
		
		editor.commit();
		
		super.onPause();
	}
}
