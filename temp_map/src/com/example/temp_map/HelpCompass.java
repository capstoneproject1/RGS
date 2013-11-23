package com.example.temp_map;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class HelpCompass {
	
	List<LatLng> temp_dest;
	
	public HelpCompass(Double from_lat, Double from_lng, Double to_lat, Double to_lng){
		temp_dest = new ArrayList<LatLng>();
		
		Double x1 = from_lng;	
    	Double y1 = from_lat;	
    	Double x2 = to_lng;		
    	Double y2 = to_lat;		
    	Double ortho_slope = (x1-x2)/(y2-y1);
    	Double dx = Math.sqrt(1/(1+(ortho_slope*ortho_slope)));
    	Double dy = ortho_slope * dx;
    	dx *= 0.00001;
    	dy *= 0.00001;
    	for(int i=1; i<9; i++){
    		Double temp_x = dx * i;
    		Double temp_y = dy * i;
    		//ADD ORDER
    		temp_dest.add(new LatLng(y2 + temp_y, x2 + temp_x));
    		temp_dest.add(new LatLng(y2 - temp_y, x2 - temp_x));
    	}
	}
	
	public Location getBearingDest(Location my){
		Location temp = new Location("newdest");
		Double result_lat = 0.0;
		Double result_lng = 0.0;
		float std_dist = -1;
		
		for(int i=0; i<16; i++){
			LatLng temp_ll = temp_dest.get(i);
			temp.setLatitude(temp_ll.latitude);
			temp.setLongitude(temp_ll.longitude);
			float new_dist = my.distanceTo(temp);
			if(std_dist < 0 || std_dist > new_dist){
				std_dist = new_dist;
				result_lat = temp_ll.latitude;
				result_lng = temp_ll.longitude;
			}
		}
		
		temp.setLatitude(result_lat);
		temp.setLatitude(result_lng);
		return temp;
	}

	public List<LatLng> getTemp_dest() {
		return temp_dest;
	}
}
