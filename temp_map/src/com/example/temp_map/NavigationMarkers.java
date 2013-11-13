package com.example.temp_map;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class NavigationMarkers {
	
	//check value for going next
	private boolean walkingToNext = false;
	
	//Go west
	private String announce;
	
	//distance & time
	private String dist;
	private String time;
	
	//TO
	private String to_lat;
	private String to_lng;
	
	//FROM
	private String from_lat;
	private String from_lng;
	
	//Smooth Path
	private LocQueue<LatLng> smoothLoc = null;
	
	public NavigationMarkers(String announce, String dist, String time, 
			String to_lat, String to_lng, String from_lat, String from_lng){
		this.announce = announce;
		this.dist = dist;
		this.time = time;
		this.to_lat = to_lat;
		this.to_lng = to_lng;
		this.from_lat = from_lat;
		this.from_lng = from_lng;
	}

	public boolean isWalkingToNext() {
		return walkingToNext;
	}
	
	public int setTrueWalkingToNext() {
		walkingToNext = true;
		
		if(smoothLoc == null){
			return -1;
		}
		
		return smoothLoc.size();
	}

	//check for smooth way
	public boolean isSmoothWay(){
		if(smoothLoc == null)
			return false;
		else if(smoothLoc.size() == 0)
			return false;
		else
			return true;
	}
	
	//add location in smoothLoc List
	public boolean addSLM(LatLng e){
		return smoothLoc.add(e);
	}
	
	//delete location in smoothLoc List
	public LatLng getSLM(int i){
		return smoothLoc.get(i);
	}
	
	public LocQueue<LatLng> getSmoothLoc() {
		return smoothLoc;
	}

	public void setSmoothLoc(LocQueue<LatLng> smoothLoc) {
		this.smoothLoc = smoothLoc;
	}

	public String getAnnounce() {
		return announce;
	}

	public void setAnnounce(String announce) {
		this.announce = announce;
	}

	public String getDist() {
		return dist;
	}

	public void setDist(String dist) {
		this.dist = dist;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTo_lat() {
		return to_lat;
	}

	public void setTo_lat(String to_lat) {
		this.to_lat = to_lat;
	}

	public String getTo_lng() {
		return to_lng;
	}

	public void setTo_lng(String to_lng) {
		this.to_lng = to_lng;
	}

	public String getFrom_lat() {
		return from_lat;
	}

	public void setFrom_lat(String from_lat) {
		this.from_lat = from_lat;
	}

	public String getFrom_lng() {
		return from_lng;
	}

	public void setFrom_lng(String from_lng) {
		this.from_lng = from_lng;
	}
	
	
}
