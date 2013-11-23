package com.example.temp_map;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ShowMap extends FragmentActivity implements SensorEventListener, OnInitListener, OnTouchListener{

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
	TextView lcl;
	int calltime = 0;
	
	//For Case of no result in navigation
	private boolean resultTag = true;
	
	//Location Listener for GPS and Network
	LocationListener gpsll;
	LocationListener netll;
	
	// for compass and bearing
	private ImageView arrowImg;
	private float curDeg = 0f;
	private SensorManager mSensorManager;
	TextView tvHeading;
	float currentDegree = 0;
	float heading;
	float bearing;
	
	//Bearing Location
	private Double bearingLat, bearingLng;
	
	GeomagneticField geoField;
	
	//Variable for Text to Speech
	String talk;
	private TextToSpeech tts;
	
	//Queue of navigationmarkers
	private LocQueue<NavigationMarkers> mylocqueue = null;
	
	//for long press events
	private boolean longpressed=false;
	boolean clickActive = false;
	long startClickTime = 0;
	private final int MIN_CLICK_DURATION = 1000;
	
	//vibration class
	Vibrator v;
	
	//option for not fast search (original search)
	private final int NEWSEARCH = -1;
	int group_opt;
	int child_opt = -1;
	
	//variable for fast search
	HelpFastSearch hfs;
	
	//variables for test (sensor detection or not)
	ToggleButton tb;
	boolean s_on = false;
	boolean set_std = false;
	float std_distance = -1;
	
	//variables for accurate direction
	HelpCompass hc = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);
        
        tv = (TextView)findViewById(R.id.inform);
        tv.setOnTouchListener(this);
        lc = (TextView)findViewById(R.id.logcat);
        lcl = (TextView)findViewById(R.id.logcatl);
        dest = getIntent().getExtras().getString("dest");
        tb = (ToggleButton) findViewById(R.id.toggleButton1);
        
        group_opt = getIntent().getExtras().getInt("group_option");
        if(group_opt != NEWSEARCH)
        	child_opt = getIntent().getExtras().getInt("child_option");
        
        System.out.println(dest);
        
        //heading
 		arrowImg = (ImageView) findViewById(R.id.imageViewArrow);
// 		arrowImg.setVisibility(View.INVISIBLE);
 		Log.e("Visibility: ", String.valueOf(arrowImg.getVisibility()));
 		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
 		//vibration
		v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		tb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				s_on = tb.isChecked();
			}
		});
		
        GooglePlayServicesUtil.isGooglePlayServicesAvailable(ShowMap.this);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(isGPSEnabled == true){
        	requestLocAndMap();
        }else{
        	setGPS();
        }
    }
    
    //All Jobs for onResume and onCreate
    public void requestLocAndMap(){
    	requestLoc();
    	System.out.println("GPS: "+ LocationManager.GPS_PROVIDER);
    	System.out.println("Network: "+ LocationManager.NETWORK_PROVIDER);
    	temploc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	if(tempMap == null){
    		setTempMap();
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
    
    
    //When we get GPS Permission from User
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
				requestLoc();
				System.out.println("setTempMap Called in onActivityResult");
		        setTempMap();
			}
			break;
		}
	}
    
    //Setting Google Map
    public void setTempMap(){
    	
    	if (tempMap == null) {
    		tempMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
    		
	    	if(tempMap != null){
	    		tempMap.setMyLocationEnabled(true);
				temploc = tempMap.getMyLocation();
				tempMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
		            @Override
		            public void onInfoWindowClick(Marker marker) {
		            	System.out.println("touch it!");
		            	speak(marker.getTitle());
		            	lc.setText(String.valueOf(marker.getPosition().latitude)+", "+ 
		            			String.valueOf(marker.getPosition().longitude));
		            }
		        });
	    	}
    	}
    }
    
    //differentiate GPS and Network and get location listener
    public void requestLoc(){
    	gpsll = new LocationListener(){

			@Override
			public void onLocationChanged(Location arg0) {
				// TODO Auto-generated method stub
				System.out.println("GPS working: outside");
				realOnLoctionChanged(arg0, true);
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
    	};
    	netll = new LocationListener(){

			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				System.out.println("Network working: indoor");
				realOnLoctionChanged(location, false);
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}
    	};
    	
    	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsll);
    	lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, netll);
    }
    
    @Override
	public void onBackPressed() {
		this.finish();
	}

    @Override
    protected void onResume() {
        super.onResume();
        requestLocAndMap();
//        mSensorManager.registerListener(this,
//				mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
//				SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        lm.removeUpdates(gpsll);
        lm.removeUpdates(netll);
        mSensorManager.unregisterListener(this);
    }
    
    //From now location, right before marker's latitude and longitude
    private Double prev_lat = 0.0;
    private Double prev_lng = 0.0;
    
    //REAL onLocationChanged
    public void realOnLoctionChanged(Location arg0, boolean isGPS){
    	calltime ++;
    	
		if(resultTag == false)
			return;
		
		double lat =  arg0.getLatitude();
        double lng = arg0.getLongitude();
        loc = new LatLng(lat, lng);
        
        geoField = new GeomagneticField(Double.valueOf(lat)
				.floatValue(),
				Double.valueOf(lng).floatValue(), Double
						.valueOf(arg0.getAltitude()).floatValue(),
				System.currentTimeMillis());
        
		if(locTag == 1){
			tempMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
	        tempMap.addMarker(new MarkerOptions().position(loc));
	        
	        String fileName = "file.txt";
			hfs = new HelpFastSearch(fileName);
			FastSearch fs = hfs.loadFS();
			
			//Keyboard search case
	        if(group_opt == NEWSEARCH){
	        	fd = new FindDest(dest, loc);
		        List<SearchMarkers> mk = fd.reqDest();
		        showMarkers(mk);
		        applyLog(fs, mk.get(0));
		        findRoad(mk.get(0));
	        }
	        //Log search (FastSearch) case
	        else{
	        	SearchMarkers certain_loc = returnFastLoc(fs, group_opt, child_opt);
	        	showMarker(certain_loc);
	        	findRoad(certain_loc);
	        }
	        
	        
	        if(mylocqueue.size()>0 && isGPS==true){
	        	speak(mylocqueue.get(0).getAnnounce());
	        	setBearingSt(lat, lng);
			}else if(mylocqueue.size()>0 && isGPS==false){
	        	speak("Go outside! Refer the direction to next marker.");
	        	setBearingSt(lat, lng);
			}else{
	        	speak("No result");
	        	resultTag = false;
	        	return;
	        }
	        locTag = 0;
		}else{
			// heading += geoField.getDeclination();
			compareLocation(arg0, mylocqueue);
		}
		if(hc != null){
			Location realBearing = hc.getBearingDest(arg0);
	        bearing = arg0.bearingTo(realBearing);
			float dist = arg0.distanceTo(realBearing);
			lcl.setText(Float.toString(dist)+" => ("+
			Double.toString(realBearing.getLatitude())+", "+
			Double.toString(realBearing.getLongitude())+")");
		}
    }
    
    //set bearing for step marker
    public void setBearingSt(Double lat, Double lng){
    	bearingLat = Double.parseDouble(mylocqueue.get(0).getFrom_lat()); 
    	bearingLng = Double.parseDouble(mylocqueue.get(0).getFrom_lng());
    	hc = new HelpCompass(lat, lng, bearingLat, bearingLng);
    	List<LatLng> tm = hc.getTemp_dest();
    	for(int i=0; i<16; i++){
    		tempMap.addMarker(new MarkerOptions().position(tm.get(i)));
    	}
    }
    
    //set bearing for smooth marker
    public void setBearingSm(Double lat, Double lng, NavigationMarkers nm){
		bearingLat = nm.getSmoothLoc().get(0).latitude;
		bearingLng = nm.getSmoothLoc().get(0).longitude;
		hc = new HelpCompass(lat, lng, bearingLat, bearingLng);
		List<LatLng> tm = hc.getTemp_dest();
    	for(int i=0; i<16; i++){
    		tempMap.addMarker(new MarkerOptions().position(tm.get(i)));
    	}
    }
    
    
    //With FastSearch, it can return destination without Search API
    public SearchMarkers returnFastLoc(FastSearch fs, int group_opt, int child_opt){
    	SearchMarkers certain_loc;
    	
    	switch(group_opt){
    	//myHome
    	case 0:
    		certain_loc = fs.getMyHome();
    		break;
    	//myWork
    	case 1:
    		certain_loc = fs.getMyWork();
    		break;
    	//myRecent
    	case 2:
    		//Totally different indexing system...
    		int myrec_len = fs.getMyRecent().size();
    		certain_loc = fs.getMyRecent().get(myrec_len-child_opt-1);
    		break;
    	//myRank
    	case 3:
    		certain_loc = fs.getMyRank().get(child_opt);
    		break;
    	default:
    		certain_loc = null;	
    	}
    	
    	return certain_loc;
    }
    
    //Applying new search result one
    public void applyLog(FastSearch fs, SearchMarkers dest){
    	fs.searchExec(dest);
		hfs.saveFS(fs); 
    }
    
    //Find Navigation from my location to destination
	public void findRoad(SearchMarkers dest){
		double lat = Double.parseDouble(dest.getLat());
		double lng = Double.parseDouble(dest.getLng());
		
		LatLng dest_loc = new LatLng(lat, lng);
		Navigation nv = new Navigation(loc, dest_loc);
		mylocqueue = nv.reqNavigation();
		showNMMarkers(loc, dest_loc, mylocqueue);
	}
	
	//Show single marker
	public void showMarker(SearchMarkers sm){
		String lat = sm.getLat();
		String lng = sm.getLng();
		LatLng temp_loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
		MarkerOptions mo = new MarkerOptions();
		mo.position(temp_loc);
		mo.title(sm.getAddr());
		
		tempMap.addMarker(mo);
	}
	
	//Show all markers of destination in list
	public void showMarkers(List<SearchMarkers> mk){
		String lat;
		String lng;

		if(mk.size()<1){
			return;
		}
//		for(int i=0; i<mk.size(); i++){
			SearchMarkers tempsm = mk.get(0);
			lat = tempsm.getLat();
			lng = tempsm.getLng();
			LatLng temp_loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
			MarkerOptions mo = new MarkerOptions();
			mo.position(temp_loc);
			mo.title(tempsm.getAddr());
			
			tempMap.addMarker(mo);
			
//		}
	}
	
	//show all Navigation Markers
	public void showNMMarkers(LatLng src, LatLng dst, LocQueue<NavigationMarkers> mylocqueue){
		String lat;
		String lng;
		int len = mylocqueue.size();
		PolylineOptions plo = new PolylineOptions();
		LocQueue<LatLng> points = new LocQueue<LatLng>();
		
		for(int i=0; i<len; i++){
			NavigationMarkers nm = mylocqueue.get(i);
			lat = nm.getFrom_lat();
			lng = nm.getFrom_lng();
			LatLng temp_loc = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
			MarkerOptions mo = new MarkerOptions();
			mo.position(temp_loc);
			mo.title(nm.getAnnounce());
			tempMap.addMarker(mo);
			points.add(temp_loc);
			if(nm.isSmoothWay()){
				LocQueue<LatLng> smo_list = nm.getSmoothLoc();
				int tmpsmolistsize = smo_list.size();
				for(int j=0; j<tmpsmolistsize; j++){
					MarkerOptions tmpmo = new MarkerOptions();
					tmpmo.position(smo_list.get(j));
					tmpmo.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
					tmpmo.title(String.valueOf(j)+": index");
					tempMap.addMarker(tmpmo);
					points.add(smo_list.get(j));
				}
			}
		}
		points.add(dst);
		plo.addAll(points);
		plo.color(Color.RED);
		plo.width(5);
		tempMap.addPolyline(plo);
	}
	
	//checking whether meeting marker or not
	private boolean meetMarker = false;
	
	//compare now location with step markers and smooth markers
	public void compareLocation(Location now_arg0, LocQueue<NavigationMarkers> mylocqueue){
		int mlqsize = mylocqueue.size();
		
		//checking all step markers and all smooth markers of first step marker
		for(int i=0; i<mlqsize; i++){
			NavigationMarkers nm = mylocqueue.get(i);
			
			//only first step marker, and if user pass announcement location
			if(nm.isWalkingToNext() && i==0){
				//checking smooth markers
				int gslsize = nm.getSmoothLoc().size();
				for(int j=0; j<gslsize; j++){
					LatLng temp = nm.getSmoothLoc().get(j);
					
					//Exact Range
					if(checkNear(now_arg0, temp, null)){
						prev_lat = temp.latitude;
						prev_lng = temp.longitude;
						
						//delete
						if(nm.getSmoothLoc().size() == j+1){
							mylocqueue.remove();
							if(mylocqueue.size() > 0){
								setBearingSt(prev_lat, prev_lng);
							}
						}else{
							for(int k=0; k<=j; k++)
								nm.getSmoothLoc().remove();
							setBearingSm(prev_lat, prev_lng, nm);
						}
						meetMarker = true;
						break;
					}
				}
			}
			
			//false => not smooth, check my from_loc (exact location included Announcement)
			//Not related in index, all checking (default all announce marker set to FALSE)
			else if(!nm.isWalkingToNext()){
				//checking step markers
				//Exact Range
				if(checkNear(now_arg0, null, nm)){
					speak(nm.getAnnounce());
					prev_lat = Double.parseDouble(nm.getFrom_lat());
					prev_lng = Double.parseDouble(nm.getFrom_lng());
					if(nm.setTrueWalkingToNext() <= 0){
						//-1: smoothLoc == NULL => next one
						//0: smoothLoc has no member => next one
						
						//include itself, remove them all
						for(int k=0; k<=i; k++)
							mylocqueue.remove();
						if(mylocqueue.size() > 0){
							setBearingSt(prev_lat, prev_lng);
						}else{
							//arrival
							/*
							 * some CODE needed. About BEARING
							 */
						}
						meetMarker = true;
					}else{
						//there is smoothLoc's member
						
						//exclude itself, remove them all
						for(int k=0; k<i; k++)
							mylocqueue.remove();
						setBearingSm(prev_lat, prev_lng, nm);
						meetMarker = true;
					}
				}
			}
			
			//When you get some Marker, you DON'T NEED to check other one
			if(meetMarker == true){
				meetMarker = false;
				break;
			}
		}
	}
	
	//2 cases (Smooth Marker or Step Marker)
	//return near or not
	public boolean checkNear(Location my, LatLng smooth, NavigationMarkers step){
		float dist = returnDist(my, smooth, step);
		if(dist == -1){
			Log.e("ERR", "my, smooth, step are all set");
			return false;
		}
		
		return dist < 8.0;
	}
		
	//return distance from my location
	public float returnDist(Location my, LatLng smooth, NavigationMarkers step){
		//step case
		if(smooth == null){
			String step_lat = step.getFrom_lat();
			String step_lng = step.getFrom_lng();
			Location step_loc = new Location("newdest");
			step_loc.setLatitude(Double.parseDouble(step_lat));
			step_loc.setLongitude(Double.parseDouble(step_lng));
			return my.distanceTo(step_loc);
		}
		//smooth case
		else if(step == null){
			Location smooth_loc = new Location("newdest");
			smooth_loc.setLatitude(smooth.latitude);
			smooth_loc.setLongitude(smooth.longitude);
			return my.distanceTo(smooth_loc);
		} 
		return -1;
	}
	
	//speak given text
	public void speak(String tmp){
		if(tmp==null)
			return;
		tv.setText(tmp);
		
		this.talk = tmp;
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, 0);
		tts = new TextToSpeech(this,this);
	}
	
	//active compass
	@Override
	public boolean onTouch(View arg0, MotionEvent e) {
		
		switch (e.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (clickActive == false) {
				Log.i(SENSOR_SERVICE, "Down clicked");
				clickActive = true;
				startClickTime = Calendar.getInstance().getTimeInMillis();
				Log.i(SENSOR_SERVICE, "Down clicked"+startClickTime);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (clickActive == true) {
				long clickDuration = Calendar.getInstance().getTimeInMillis()
						- startClickTime;
				Log.i(SENSOR_SERVICE, "Move clicked "+clickDuration);
				if (clickDuration >= MIN_CLICK_DURATION) {
					longpressed	= true;
					mSensorManager.registerListener(this,
							mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
							SensorManager.SENSOR_DELAY_GAME);
					v.vibrate(500);
					runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							arrowImg.setVisibility(View.VISIBLE);
						}
						
					});
					clickActive = false;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			clickActive = false;
			Log.e("UP", String.valueOf(longpressed));
			if (longpressed){
				mSensorManager.unregisterListener(this);
				runOnUiThread(new Runnable(){
					@Override
					public void run() {
						// TODO Auto-generated method stub
						arrowImg.setVisibility(View.INVISIBLE);
						System.out.println("did!");
					}
				});
				longpressed=false;
				Log.e("Visibility: ", String.valueOf(arrowImg.getVisibility()));
				Log.e("Visibility: ", String.valueOf(View.INVISIBLE));
			}
			break;

		}
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		//east = 0, west = 180, south = 90, north = 270
		if(arrowImg.getVisibility() == View.VISIBLE){
			float degree = Math.round(arg0.values[0]);
			heading = Math.round(arg0.values[0])+90;
			degree = (bearing - heading) * -1;
			//degree = Math.round(arg0.values[0]);
			lc.setText("Heading result: " + Float.toString(heading) + " degrees "
					+ "Bearing result: " + Float.toString(bearing) + " degrees"
					+ "Degree result: " + Float.toString(degree) + " degrees");
			// create a rotation animation (reverse turn degree degrees)
			RotateAnimation ra = new RotateAnimation(currentDegree, degree,
					Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
					0.5f);
			// how long the animation will take place
			ra.setDuration(210);
			// set the animation after the end of the reservation status
			ra.setFillAfter(true);
			// Start the animation
			arrowImg.startAnimation(ra);
			currentDegree = -degree;
		}
	}

	private float normalizeDegree(float value) {
		if (value >= 0.0f && value <= 180.0f) {
			return value;
		} else {
			return 180 + (180 + value);
		}
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		tts.setLanguage(Locale.US);
		tts.speak(talk, TextToSpeech.QUEUE_ADD,null);
	}
}