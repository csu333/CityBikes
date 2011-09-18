/*
 * Copyright (C) 2010 Lluís Esquerda
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

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;

public class StationSlidingDrawer extends SlidingDrawer {
	
	private Handler handler;
	private List <StationOverlay> stations;
	private ListView listView;
	private FrameLayout frameLayout;
	
	public static final int ITEMCLICKED = 200;
	
	private ArrayAdapter<StationOverlay> adapter;
	
	private Context context;
	
	private WindowManager wm;
	
	public StationSlidingDrawer(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initVars();
	}

	public StationSlidingDrawer(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		initVars();
	}
	
	private int getWindowHeight(){
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}
	
	public void setStations(List <StationOverlay> sts){
		stations = sts;
		adapter = new StationsAdapter(context, R.layout.stations_list_item, stations);
		listView.setAdapter(adapter);
		this.updateFrame();
	}

	public void initVars(){
		listView = new ListView(context);
		stations = new LinkedList<StationOverlay>();
		
		adapter = new StationsAdapter(context, R.layout.stations_list_item, stations);
		listView.setAdapter(adapter);
		wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);	
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				int pos = v.getId();
				if (pos != -1){
					StationOverlay selected = stations.get(position);
					if (selected != null){
						Message msg = new Message();
						msg.what = StationSlidingDrawer.ITEMCLICKED;
						msg.arg1 = selected.getPosition();
						msg.obj = selected;
						handler.sendMessage(msg);
						int height = arg0.getHeight();
						if (height > getWindowHeight() / 2) {
							animateClose();
						}
					}
				}
				
			}
		});
		listView.setBackgroundColor(Color.BLACK);
		listView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		frameLayout = (FrameLayout) findViewById(R.id.content);
		if (frameLayout != null){
			frameLayout.setBackgroundColor(Color.BLACK);
			frameLayout.removeAllViews();
			frameLayout.addView(listView);
		}
		listView.setAdapter(adapter);
		
	}
	public void updateFrame(){
		frameLayout = (FrameLayout) findViewById(R.id.content);
		if (frameLayout != null){
			frameLayout.setBackgroundColor(Color.BLACK);
			frameLayout.removeAllViews();
			frameLayout.addView(listView);
		}
	}
	
	public void setHandler (Handler h){
		handler = h;
	}
}

