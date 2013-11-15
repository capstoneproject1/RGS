package com.example.temp_map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class RankMarkers extends SearchMarkers{

	private static final long serialVersionUID = 1L;
	private int callCount;
	
	public RankMarkers(String addr, String lat, String lng) {
		super(addr, lat, lng);
		// TODO Auto-generated constructor stub
		callCount = 1;
	}

	public RankMarkers(SearchMarkers sm){
		super(sm.getAddr(), sm.getLat(), sm.getLng());
		callCount = 1;
	}
	
	protected void writeObject(ObjectOutputStream stream)
            throws IOException {
		super.writeObject(stream);
        stream.writeInt(callCount);
    }

	protected void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
		super.readObject(stream);
        callCount = stream.readInt();
    }
	
	public void call(){
		callCount++;
	}
	
	public int getCallCount(){
		return callCount;
	}
}
