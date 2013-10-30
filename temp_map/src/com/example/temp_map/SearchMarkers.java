package com.example.temp_map;

public class SearchMarkers {
	//Address full name
	private String addr;
	
	//Latitude and Longitude
	private String lat;
	private String lng;
	
	public SearchMarkers(String addr, String lat, String lng){
		this.addr = addr;
		this.lat = lat;
		this.lng = lng;
	}
	
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLng() {
		return lng;
	}
	public void setLng(String lng) {
		this.lng = lng;
	}
	
	
}
