package net.homelinux.penecoptero.android.citybikes.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.EditText;
import android.widget.TextView;

public class AccountActivity extends Activity {

	private EditText editText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account);
		editText = (EditText) findViewById(R.id.editText);

		SharedPreferences settings = getSharedPreferences(CityBikes.PREFERENCES_NAME,0);

		String subscriberPIN = settings.getString("subscriberPIN", null);

		// If subscriber PIN not known, shows the configuration screen
		if (subscriberPIN == null || subscriberPIN.equals("")){
			startActivity(new Intent(this, AccountSettingsActivity.class));
		} 

	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences settings = getSharedPreferences(CityBikes.PREFERENCES_NAME,0);

		String city = settings.getString("city", null);
		String subscriberNumber = settings.getString("subscriberNumber", null);
		String subscriberName = settings.getString("subscriberName", null);
		String subscriberPIN = settings.getString("subscriberPIN", null);
		
		// If subscriber PIN not known, close activity
		if (subscriberPIN == null || subscriberPIN.equals("")){
			finish();
		}
		
		JSONObject accountInfo = 
			new AccountHelper().getSubscriberAccountInfo(
					city, subscriberNumber, subscriberName, subscriberPIN);
		editText.setText(new AccountHelper().getSubscriberAccountInfo(
				city, subscriberNumber, subscriberName, subscriberPIN).toString());

		try {
			// Account info
			TextView firstname = (TextView) findViewById(R.id.firstname);
			firstname.setText(accountInfo.getString("firstname"));

			TextView lastname = (TextView) findViewById(R.id.lastname);
			lastname.setText(accountInfo.getString("lastname"));

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
			Date expiryDate = sdf.parse(accountInfo.getString("endValidity"));
			expiryDate.setTime(expiryDate.getTime() + 
					TimeZone.getDefault().getOffset(expiryDate.getTime()));
			long daysToGo = expiryDate.getTime() - System.currentTimeMillis();				

			TextView accountValidity = (TextView) findViewById(R.id.account_validity);
			accountValidity.setText(String.format(getString(R.string.days_to_go), 
					expiryDate.toLocaleString(),
					(daysToGo / DateUtils.DAY_IN_MILLIS)));	

			TextView accountState = (TextView) findViewById(R.id.account_state);
			accountState.setText(
					accountInfo.getString("account") + " " +
					accountInfo.getString("currency"));	


			// Trip info
			TextView inTrip = (TextView) findViewById(R.id.in_trip);
			inTrip.setText(accountInfo.getString("inTrip"));

			expiryDate = sdf.parse(accountInfo.getString("lastTripDate"));
			expiryDate.setTime(expiryDate.getTime() + 
					TimeZone.getDefault().getOffset(expiryDate.getTime()));

			TextView lastTripDate = (TextView) findViewById(R.id.last_trip_date);
			lastTripDate.setText(expiryDate.toLocaleString() + " (" +
					DateUtils.getRelativeTimeSpanString(
							expiryDate.getTime(), 
							System.currentTimeMillis(), 
							DateUtils.MINUTE_IN_MILLIS) + ")");

			TextView lastTripDuration = (TextView) findViewById(R.id.last_trip_duration);
			lastTripDuration.setText(accountInfo.getString("lastTripDuration") + " " +
					getString(R.string.minutes));

			TextView lastTripAmount = (TextView) findViewById(R.id.last_trip_amount);
			lastTripAmount.setText(
					accountInfo.getString("lastTripAmount") + " " + 
					accountInfo.getString("currency"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
