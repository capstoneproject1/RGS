package com.example.temp_map;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ShowMap extends FragmentActivity implements LocationListener {

	//Google Map Variable
	GoogleMap tempMap;
	
	LatLng loc;
	Location temploc;
	LocationManager lm;
//	String pv;
	int locTag = 1;
	int call = 0;
	
	FindDest fd;
	String dest;
	
	//Variable
	TextView tv;
	TextView lc;
	private List<NavigationMarkers> lnm;
	int calltime = 0;
	
	//For Case of no result in navigation
	private boolean resultTag = true;
	
	//Now Step Count
	private int stepCount = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);
        
        tv = (TextView)findViewById(R.id.inform);
        lc = (TextView)findViewById(R.id.logcat);
        dest = getIntent().getExtras().getString("dest");
        System.out.println(dest);
        
        GooglePlayServicesUtil.isGooglePlayServicesAvailable(ShowMap.this);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(isGPSEnabled == true){
        	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ShowMap.this);
        	lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ShowMap.this);
        	System.out.println("GPS: "+ LocationManager.GPS_PROVIDER);
        	System.out.println("Network: "+ LocationManager.NETWORK_PROVIDER);
        	temploc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        	
        	if(tempMap == null){
        		setTempMap();
        	}
        }else{
        	setGPS();
        }
    }

    //GPS New Setting
    public void setGPS(){
    	new AlertDialog.Builder(ShowMap.this)
        .setTitle("위치서비스 동의")
        .setNeutralButton("이동" ,new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
			}
		}).setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		})
        .show();
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {//위치설정 엑티비티 종료 후 
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 0:
			lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//		    Criteria c = new Criteria();
//			pv = lm.getBestProvider(c, true);
			boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			if(isGPSEnabled==false){//사용자가 위치설정동의 안했을때 종료 
				finish();
			}else{//사용자가 위치설정 동의 했을때 
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ShowMap.this);
	        	lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ShowMap.this);
				System.out.println("setTempMap Called in onActivityResult");
		        setTempMap();
			}
			break;
		}
	}
    
    public void setTempMap(){
    	System.out.println("setTempMap Called");
//    	System.out.println((pv!=null)+": pv");
    	call ++;
    	System.out.println(call);
    	if (tempMap == null) {
    		tempMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
    		System.out.println(tempMap+": TempMap");
    	
	    	if(tempMap != null){
	    		tempMap.setMyLocationEnabled(true);
				temploc = tempMap.getMyLocation();
				tempMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
		            @Override
		            public void onInfoWindowClick(Marker marker) {
		            	System.out.println("touch it!");
		            	tv.setText(marker.getTitle());
		            }
		        });
	    	}
    	}
    }
    
    @Override
	public void onBackPressed() {
		this.finish();
	}

    @Override
    protected void onResume() {
        super.onResume();
        setTempMap();
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(this);
    }
    
    
    
	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
		calltime ++;
		lc.setText("onLocationChanged Called (" + calltime + ") Times.");
		
		if(resultTag == false)
			return;
		
		double lat =  arg0.getLatitude();
        double lng = arg0.getLongitude();
        loc = new LatLng(lat, lng);
		if(locTag == 1){
			tempMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
	        tempMap.addMarker(new MarkerOptions().position(loc));
	        fd = new FindDest(dest, loc);
	        List<SearchMarkers> mk = fd.reqDest();
	        showMarkers(mk);
	        findRoad(mk.get(0));
	        locTag = 0;
	        if(lnm.size()>0)
	        	tv.setText(lnm.get(0).getAnnounce());
	        else{
	        	tv.setText("No result");
	        	resultTag = false;
	        }
		}else{
			compareLocation(loc, lnm);
		}
	}

	public void findRoad(SearchMarkers dest){
		double lat = Double.parseDouble(dest.getLat());
		double lng = Double.parseDouble(dest.getLng());
		LatLng dest_loc = new LatLng(lat, lng);
		Navigation nv = new Navigation(loc, dest_loc);
		lnm = nv.reqNavigation();
		showNMMarkers(loc, dest_loc, lnm);
	}
	
	public void showMarkers(List<SearchMarkers> mk){
		String lat;
		String lng;
//		if(tempMap==null){
//			tempMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
//		}
		if(mk.size()<1){
			return;
		}
//		for(int i=0; i<mk.size(); i++){
			SearchMarkers tempsm = mk.get(0);
			tempsm.getAddr();
			lat = tempsm.getLat();
			lng = tempsm.getLng();
			LatLng temp_loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
			MarkerOptions mo = new MarkerOptions();
			mo.position(temp_loc);
			mo.title(tempsm.getAddr());
			
			tempMap.addMarker(mo);
			
//		}
	}
	
	public void showNMMarkers(LatLng src, LatLng dst, List<NavigationMarkers> lnm){
		String lat;
		String lng;
		int len = lnm.size();
		PolylineOptions plo = new PolylineOptions();
		
		for(int i=0; i<len; i++){
			NavigationMarkers nm = lnm.get(i);
			lat = nm.getFrom_lat();
			lng = nm.getFrom_lng();
			LatLng temp_loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
			MarkerOptions mo = new MarkerOptions();
			mo.position(temp_loc);
			mo.title(nm.getAnnounce());
			tempMap.addMarker(mo);
			plo.add(temp_loc);
			
		}
		plo.add(dst);
		plo.color(Color.RED);
		plo.width(3);
		tempMap.addPolyline(plo);
	}
	
	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
    
	//compare now location with step marker
	public void compareLocation(LatLng now_loc, List<NavigationMarkers> lnm){
		int len = lnm.size();
		for(int i=stepCount; i<len; i++){
			NavigationMarkers nm = lnm.get(i);
			if(now_loc.latitude - Double.parseDouble(nm.getFrom_lat()) < 0.0001 &&
					now_loc.latitude - Double.parseDouble(nm.getFrom_lat()) > -0.0001 &&
					now_loc.longitude - Double.parseDouble(nm.getFrom_lng()) < 0.0001 &&
					now_loc.longitude - Double.parseDouble(nm.getFrom_lng()) > -0.0001){
				tv.setText(nm.getAnnounce());
				stepCount = i+1;
				break;
			}
		}
	}
	
}
