package net.homelinux.penecoptero.android.citybikes.app;

import java.util.ArrayList;

import org.json.JSONException;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
		try {
			ArrayList<StationOverlay> stations = new ArrayList<StationOverlay>();
			
			// Browse the stations to find back favorites
			// Note 1: no station will be shown if station db from main activity is empty
			// Note 2: could be more effective (O(n) instead of O(n²) if station list was sorted
			StationsDBAdapter sda = MainActivity.getStationDBAdapter();
			for (StationOverlay station : sda.getMemory()){
				Station s = station.getStation();
				if (s.isBookmarked()){
					s.getId();
					stations.add(station);
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
	        MainActivity.setBookmarkedStation((int)info.id, false);
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
}
