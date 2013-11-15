package com.example.temp_map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

public class SearchMarkers  implements Serializable{

	private static final long serialVersionUID = 1L;
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
	
	protected void writeObject(ObjectOutputStream stream)
            throws IOException {
        stream.writeObject(addr);
        stream.writeObject(lat);
        stream.writeObject(lng);
    }

	protected void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        addr = (String) stream.readObject();
        lat = (String) stream.readObject();
        lng = (String) stream.readObject();
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
