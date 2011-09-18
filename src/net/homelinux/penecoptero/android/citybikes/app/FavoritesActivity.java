package net.homelinux.penecoptero.android.citybikes.app;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class FavoritesActivity extends ListActivity {

	private static final int MENU_ITEM_SYNC = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST;

	private StationsAdapter mAdapter;
	private ListView mList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stations_list);
		mList = (ListView) findViewById(android.R.id.list);
		fillData();
		registerForContextMenu(mList);
	}

	/**
	 * Fills station list from MainActivity Station DB Adapter, 
	 * showing only those saved a favorites 
	 */
	public void fillData(){
		SharedPreferences settings = getSharedPreferences(CityBikes.PREFERENCES_NAME,0);
		JSONArray favorites;
		try {
			favorites = new JSONArray(settings.getString("favorites", "[]"));
			ArrayList<StationOverlay> stations = new ArrayList<StationOverlay>();
			
			// Browse the stations to find back favorites
			// Note 1: no station will be shown if station db from main activity is empty
			// Note 2: could be more effective (O(n) instead of O(n²) if station list was sorted
			StationsDBAdapter sda = MainActivity.getStationDBAdapter();
			for (StationOverlay station : sda.getMemory()){
				Station s = station.getStation();
				for (int i = 0 ; i < favorites.length() ; i++){
					int id = favorites.optInt(i, -1);
					// Some old null references might stay in the array, skip them 
					if (id == -1){
						break;
					}
					if (s.getId() == id){
						stations.add(station);
						break;
					}
				}
			}
			
			// Set the favorite station list to be shown
			mAdapter = new StationsAdapter(getApplication(), 
					R.layout.stations_list_item, stations);
			mList.setAdapter(mAdapter);
			
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}
    
    @Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
    	case DELETE_ID:
    		// Removes station from preferences then refresh list
    		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	        removeFromFavorites(info.id);
	        fillData();
	        return true;
		}
		return super.onContextItemSelected(item);
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
				MainActivity.progressDialog = new ProgressDialog(this);
				MainActivity.progressDialog.setTitle("");
				MainActivity.progressDialog.setMessage(getString(R.string.loading));
				MainActivity.progressDialog.show();
				
				MainActivity.getStationDBAdapter().sync(true, new Bundle());
				// This is an ugly way of updating list
				MainActivity.progressDialog.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						fillData();
					}
				});
			} catch (Exception e) {

			}
			return true;
		}

		return super.onOptionsItemSelected(item);
    }

    /**
     * Removes station with ID stationId from application preferences 
     * @param stationId
     */
	private void removeFromFavorites(long stationId) {
		SharedPreferences settings = getApplicationContext().getSharedPreferences(
				CityBikes.PREFERENCES_NAME,0);
		SharedPreferences.Editor editor = settings.edit();
		JSONArray favorites;
		try {
			favorites = new JSONArray(settings.getString("favorites", "[]"));
			// Optimization: if only one item left in array, completely trim array
			if (favorites.isNull(1)){
				editor.putString("favorites", "[]");
			} else {
				// Remove station id
				int i = 0;
				// Find value in favorites
				while (favorites.getInt(i) < stationId){
					i++;
				}
				// Overwrite value by shifting higher end of array left
				while (i < favorites.length() - 1){
					favorites.put(i, favorites.get(i + 1));
					i++;
				}
				// This is some weird implementation of Google:
				// there is no remove() method for JSONArray in Android API
				// The recommended way if to put null instead but it doesn't
				// affect the way the array is put into String nor its length
				favorites.put(i, null);
				
				editor.putString("favorites", favorites.toString());
			}
			editor.commit();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
