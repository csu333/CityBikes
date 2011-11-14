package net.homelinux.penecoptero.android.citybikes.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class AccountActivity extends Activity {
	static final int PROGRESS_DIALOG = 0;
	public static final int MENU_ITEM_SYNC = Menu.FIRST;
	private ProgressDialog progressDialog;
	AccountHelper mAccountHelper = new AccountHelper();

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

		String subscriberPIN = settings.getString("subscriberPIN", null);

		// If subscriber PIN not known, close activity
		if (subscriberPIN == null || subscriberPIN.equals("")){
			finish();
		} else {
			// Load info in background
			showDialog(PROGRESS_DIALOG);
		}
	}

	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case PROGRESS_DIALOG:
			progressDialog = new ProgressDialog(AccountActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setIndeterminate(true);
			progressDialog.setMessage(getString(R.string.loading));
			return progressDialog;
		default:
			return null;
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				JSONObject accountInfo = (JSONObject)msg.obj;
				dismissDialog(PROGRESS_DIALOG);
				showAccountInfo(accountInfo);
			}
		};

		switch(id) {
		case PROGRESS_DIALOG:
			progressDialog.setProgress(0);

			Thread progressThread = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					JSONObject result = fetchAccountInfo();
					Message msg = new Message();
					msg.obj = result;
					handler.sendMessage(msg);
				}
			});
			progressThread.start();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_ITEM_SYNC, 0, R.string.menu_sync).setIcon(
				R.drawable.ic_menu_refresh);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_SYNC:
			try {
				// Load info in background
				showDialog(PROGRESS_DIALOG);
			} catch (Exception e) { }
			return true;
		}

		return super.onOptionsItemSelected(item);
	}


	/**
	 * Fetches the account information using log in information stored in preferences
	 * @return The JSON Object received from the server
	 */
	private JSONObject fetchAccountInfo(){
		SharedPreferences settings = getSharedPreferences(CityBikes.PREFERENCES_NAME,0);

		String city = settings.getString("city", null);
		String subscriberNumber = settings.getString("subscriberNumber", null);
		String subscriberName = settings.getString("subscriberName", null);
		String subscriberPIN = settings.getString("subscriberPIN", null);
		
		JSONObject accountInfo = 
			mAccountHelper.getSubscriberAccountInfo(
					city, subscriberNumber, subscriberName, subscriberPIN);

		return accountInfo;
	}

	/**
	 * Show account information on screen
	 * @param accountInfo The account information as received from the server
	 */
	private void showAccountInfo(JSONObject accountInfo){
		editText.setText(accountInfo.toString());

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
			Boolean inTripValue = Boolean.parseBoolean(accountInfo.getString("inTrip"));
			ImageView imageView = (ImageView) findViewById(R.id.in_trip);
			if (inTripValue){
				imageView.setImageResource(R.drawable.bike_sign);
			} else {
				imageView.setImageResource(R.drawable.parking);
			}

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
