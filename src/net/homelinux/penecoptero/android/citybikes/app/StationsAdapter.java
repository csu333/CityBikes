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

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

class StationsAdapter extends ArrayAdapter<StationOverlay> {

	private int black;
	private int green;
	private int yellow;
	private int red;
	
	private LayoutInflater mInflater;
	
	public StationsAdapter(Context context, int textViewResourceId,
			List <StationOverlay> objects) {
		super(context, textViewResourceId, objects);
		
		black = R.drawable.black_gradient;
		green = R.drawable.green_gradient;
		yellow = R.drawable.yellow_gradient;
		red = R.drawable.red_gradient;
		
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row;
		if (convertView == null){
			row = mInflater.inflate(R.layout.stations_list_item, null);
		}else{
			row = convertView;
		}
		StationOverlay tmp = (StationOverlay) getItem(position);
		TextView stId = (TextView) row
			.findViewById(R.id.station_list_item_id);
		stId.setText(tmp.getStation().getName());
		TextView stOc = (TextView) row
			.findViewById(R.id.station_list_item_ocupation);
		stOc.setText(tmp.getStation().getOcupation());
		TextView stDst = (TextView) row
			.findViewById(R.id.station_list_item_distance);
		stDst.setText(tmp.getStation().getDistance());
		TextView stWk = (TextView) row
			.findViewById(R.id.station_list_item_walking_time);
		stWk.setText(tmp.getStation().getWalking());

		int bg;
		switch (tmp.getState()) {
		case StationOverlay.BLACK_STATE:
			bg = black;
			break;
		case StationOverlay.GREEN_STATE:
			bg = green;
			break;
		case StationOverlay.RED_STATE:
			bg = red;
			break;
		case StationOverlay.YELLOW_STATE:
			bg = yellow;
			break;
		default:
			bg = R.drawable.fancy_gradient;
		}
		LinearLayout sq = (LinearLayout) row
			.findViewById(R.id.station_list_item_square);
		sq.setBackgroundResource(bg);
		row.setId(tmp.getStation().getId());
		return row;
	}
}
