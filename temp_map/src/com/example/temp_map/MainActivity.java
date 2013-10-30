package com.example.temp_map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	TextView destText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_dest);
        
        Button destBtn = (Button) findViewById(R.id.destButton);
        destText = (TextView) findViewById(R.id.destText);
        destBtn.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		String destination = destText.getText().toString();
        		Intent intent = new Intent(MainActivity.this, ShowMap.class);
        		intent.putExtra("dest", destination);
                startActivity(intent);
        	}
        });
	}
}

//class ShowMap extends FragmentActivity implements LocationListener {
//
//	GoogleMap tempMap;
//	LatLng loc;
//	Location temploc;
////	LatLng saddr = new LatLng(37, 128);
////	LatLng daddr = new LatLng(37.5, 128.5);
////	JSONObject retroad;
//	LocationManager lm;
//	String pv;
//	int locTag = 1;
//	int call = 0;
//	
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        
//        setContentView(R.layout.activity_google_map);
//        
//        GooglePlayServicesUtil.isGooglePlayServicesAvailable(ShowMap.this);
//        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        Criteria c = new Criteria();
//        pv = lm.getBestProvider(c, true);
//        
//        temploc = lm.getLastKnownLocation(pv);
//        //System.out.println(temploc.getLatitude()+", "+temploc.getLongitude());
//        //TempMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 5));
//        //TempMap.addMarker(new MarkerOptions().position(loc));
//        
//        
//        if(pv!=null){
//        	lm.requestLocationUpdates(pv, 1, 1, ShowMap.this);
////        	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, MainActivity.this);
////        	lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, MainActivity.this);
//        	if(tempMap == null){
//        		setTempMap();
//        	}
//        }else{
//        	setGPS();
//        }
////        final Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&sensor=false"));
////        final Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse("http://maps.google.com/maps?" + "saddr="+ saddr.latitude + "," + saddr.longitude + "&daddr=" + daddr.latitude + "," + daddr.longitude));
////        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
////        startActivity(intent);
//    }
//
//    public void setGPS(){
//    	new AlertDialog.Builder(ShowMap.this)
//        .setTitle("위치서비스 동의")
//        .setNeutralButton("이동" ,new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
//			}
//		}).setOnCancelListener(new DialogInterface.OnCancelListener() {
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				finish();
//			}
//		})
//        .show();
//    }
//    
//    @Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {//위치설정 엑티비티 종료 후 
//		super.onActivityResult(requestCode, resultCode, data);
//		switch (requestCode) {
//		case 0:
//			 lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//		        Criteria c = new Criteria();
//		        pv = lm.getBestProvider(c, true);
//		        if(pv==null){//사용자가 위치설정동의 안했을때 종료 
//					finish();
//			}else{//사용자가 위치설정 동의 했을때 
//				lm.requestLocationUpdates(pv, 1L, 2F, ShowMap.this);
//				System.out.println("setTempMap Called in onActivityResult");
//		        setTempMap();
//			}
//			break;
//		}
//	}
//    
//    public void setTempMap(){
//    	System.out.println("setTempMap Called");
//    	System.out.println((pv!=null)+": pv");
//    	call ++;
//    	System.out.println(call);
//    	if (tempMap == null) {
//    		tempMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
//    		System.out.println(tempMap+": TempMap");
//    	
//	    	if(tempMap != null){
//	    		System.out.println("No problem");
//	    		tempMap.setMyLocationEnabled(true);
//				temploc = tempMap.getMyLocation();
//				System.out.println("location set :"+temploc);
//				System.out.println("location enable? :"+tempMap.isMyLocationEnabled());
//	    	}
//    	}
//    }
//    
//    @Override
//	public void onBackPressed() {
//		this.finish();
//	}
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        setTempMap();
//        
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        lm.removeUpdates(this);
//    }
//    
//	@Override
//	public void onLocationChanged(Location arg0) {
//		// TODO Auto-generated method stub
//		System.out.println("onLocationChanged Called");
//		if(locTag == 1){
//			Log.d("myLog"  , "onLocationChanged: !!"  + "onLocationChanged!!");
//	        double lat =  arg0.getLatitude();
//	        double lng = arg0.getLongitude();
//	        loc = new LatLng(lat, lng);
//	        Log.e(String.valueOf(lat), String.valueOf(lng));
//	        tempMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
//	        tempMap.addMarker(new MarkerOptions().position(loc));
//	        locTag = 0;
//		}
//	}
//
//	@Override
//	public void onProviderDisabled(String arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onProviderEnabled(String arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
//		// TODO Auto-generated method stub
//		
//	}
//    
//}
