package com.example.temp_map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Navigation {
	private LatLng src_loc;
	private LatLng dest_loc;
	private HttpClient httpclient;
	private List<LatLng> smoothMk;
	private LocQueue<NavigationMarkers> lnm = null;
	
	public Navigation(LatLng src_loc, LatLng dest_loc){
		this.src_loc = src_loc;
		this.dest_loc = dest_loc;
	}
	
	public LocQueue<NavigationMarkers> reqNavigation(){
		searchNavigation sn = new searchNavigation();
		sn.start();
		while(sn.isAlive()){}
		System.out.println("THREAD IS ALL DONE");
		return lnm;
	}
	
	public List<LatLng> reqSmooth(){
		return smoothMk;
	}
	
	public class searchNavigation extends Thread{
		public URI makeParameter(LatLng src_loc, LatLng dest_loc){
			URI uri = null;
			
			//http://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&sensor=true&avoid=highways&mode=walking
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			String srcLocInfo = src_loc.latitude+","+ src_loc.longitude;
			String destLocInfo = dest_loc.latitude+","+ dest_loc.longitude; 
			params.add(new BasicNameValuePair("origin", srcLocInfo));
		    params.add(new BasicNameValuePair("destination", destLocInfo));
		    params.add(new BasicNameValuePair("sensor", "true")); //마일로 표시된 반경
		    params.add(new BasicNameValuePair("avoid", "highways")); //검색오션. 업체
		    params.add(new BasicNameValuePair("mode", "walking"));
		    
		    try {
//		    	http://maps.google.com/?near=40.4245695%2C-86.9127475&q=knoy%2Bhall&output=json&radius=3&num=5
				uri = URIUtils.createURI("http", "maps.googleapis.com", -1, "/maps/api/directions/json", URLEncodedUtils
					    .format(params, "UTF-8"), null);
				System.out.println(uri.toString());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    return uri;
		}
		
		public void run() {
			httpclient = new DefaultHttpClient();
			try {
				HttpGet get = new HttpGet();
				
				//Make Parameter
				URI sendURI = makeParameter(src_loc, dest_loc);
				
				//http://maps.googleapis.com/maps/api/directions/json?origin=40.4279243%2C-86.9111332&destination=40.424954%2C-86.910461&sensor=true&avoid=highways&mode=walking
				get.setURI(sendURI);
				
				HttpParams params = httpclient.getParams();
				params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
						HttpVersion.HTTP_1_1);
				HttpConnectionParams.setConnectionTimeout(params, 10000);
				HttpConnectionParams.setSoTimeout(params, 10000);
				httpclient.execute(get, responseSearchHandler);

			} catch (ConnectTimeoutException e) {
//				Message message = resultHandler.obtainMessage();
//				Bundle bundle = new Bundle();
//				bundle.putString(RESULT, TIMEOUT_RESULT);
//				message.setData(bundle);
//				resultHandler.sendMessage(message);
//				stringData = e.toString();
				Log.e("ConnectTimeoutException", e.toString());
			} catch (Exception e) {
//				Message message = resultHandler.obtainMessage();
//				Bundle bundle = new Bundle();
//				bundle.putString(RESULT, ERROR_RESULT);
//				message.setData(bundle);
//				resultHandler.sendMessage(message);
//				stringData = e.toString();
				Log.e("Other Exception", e.toString());
			} finally {
				httpclient.getConnectionManager().shutdown();
			}
		}
	}
	
	private ResponseHandler<String> responseSearchHandler = new ResponseHandler<String>() {

		@Override
		public String handleResponse(HttpResponse arg0)
				throws ClientProtocolException, IOException {
			// TODO Auto-generated method stub
			System.out.println("There is response!!");
			StringBuilder sb = new StringBuilder();
			try{
				InputStreamReader isr = new InputStreamReader(arg0
						.getEntity().getContent(), "EUC-KR");
				BufferedReader br = new BufferedReader(isr);
				for (;;) {
					String line = br.readLine();
					if (line == null)
						break;
					sb.append(line + '\n');
				}
				br.close();
			} catch (Exception e) {

//				Message message = resultHandler.obtainMessage();
//				Bundle bundle = new Bundle();
//				bundle.putString(RESULT, ERROR_RESULT);
//				message.setData(bundle);
//				resultHandler.sendMessage(message);
//
//				stringData = "JSon >> \n" + e.toString();
//				return stringData;
				Log.e("Response Exception", e.toString());
				return null;
			}
			
			String jsonString = sb.toString();
			System.out.println("API result: "+"\n"+jsonString);
			lnm = parseJSON(jsonString);
//			showMarkers(positions);
			return null;
		}
		
		public LocQueue<NavigationMarkers> parseJSON(String json){
			LocQueue<NavigationMarkers> result = null;
			try{
				JSONObject object = new JSONObject(json);
				JSONArray routes = object.getJSONArray("routes");
				JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
				String encodePoly = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points");
				JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");
				
				int len = steps.length();
				System.out.println(len);
				result = new LocQueue<NavigationMarkers>();
				String to_lat, to_lng, from_lat, from_lng;
				String announce;
				String dist, time;
				String encode_polyline;
				for (int i = 0; i < len; i++) {
					
					announce = steps.getJSONObject(i).getString("html_instructions");
					announce = mod(announce);
					dist = steps.getJSONObject(i).getJSONObject("distance").getString("text");
					time = steps.getJSONObject(i).getJSONObject("duration").getString("text");
					to_lat = steps.getJSONObject(i).getJSONObject("end_location").getString("lat");
					to_lng = steps.getJSONObject(i).getJSONObject("end_location").getString("lng");
					from_lat = steps.getJSONObject(i).getJSONObject("start_location").getString("lat"); 
					from_lng = steps.getJSONObject(i).getJSONObject("start_location").getString("lng"); 
					encode_polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
					LocQueue<LatLng> smoothLoc = getSmoothLoc(encode_polyline);
					NavigationMarkers nm = new NavigationMarkers(announce, dist, time, to_lat, to_lng, from_lat, from_lng);
					nm.setSmoothLoc(smoothLoc);
					result.add(nm);
					System.out.println("announce: " + announce);
					System.out.println("latitude: " + from_lat);
					System.out.println("longitude: " + from_lng);
					Log.d("polyline: ", encode_polyline);
				}
				
//				smoothMk = decodePoly(encodePoly);
			}catch (JSONException e){
				e.printStackTrace();
			}
			
			return result;
		}
		
		private LocQueue<LatLng> getSmoothLoc(String encoded){
			LocQueue<LatLng> result = new LocQueue<LatLng>();
			result = decodePoly(encoded);
			result.pop();
			result.remove();
			return result;
		}
		
		private LocQueue<LatLng> decodePoly(String encoded) {

			  LocQueue<LatLng> poly = new LocQueue<LatLng>();
			  int index = 0, len = encoded.length();
			  int lat = 0, lng = 0;

			  while (index < len) {
			      int b, shift = 0, result = 0;
			      do {
			          b = encoded.charAt(index++) - 63;
			          result |= (b & 0x1f) << shift;
			          shift += 5;
			      } while (b >= 0x20);
			      int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			      lat += dlat;

			      shift = 0;
			      result = 0;
			      do {
			          b = encoded.charAt(index++) - 63;
			          result |= (b & 0x1f) << shift;
			          shift += 5;
			      } while (b >= 0x20);
			      int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			      lng += dlng;

			      LatLng p = new LatLng(((double) lat / 1E5),
			           ((double) lng / 1E5));
			      Log.e(String.valueOf((double) lat / 1E5),
			    		  String.valueOf((double) lng / 1E5));
			      poly.add(p);
			  }

			  return poly;
			}
		
		//delete <b> and </b>, <div ~~~> and </div>
		public String mod(String anc){
			int len = anc.length();
			char [] temp_anc = anc.toCharArray();
			char [] result_anc = new char[len];
			String ch_anc = null;
			
			int i, j;
			int re_ind=0;
			
			//copy to new one
			for(i=0; i<len; i++){
				if(temp_anc[i] == '<'){
					i ++;
					//<b> </b> <div> </div> difference
					if(temp_anc[i] != 'b' && temp_anc[i] != '/'){
						result_anc[re_ind] = ' ';
						re_ind ++;
					}
					
					for(j=i; j<len; j++){
						if(temp_anc[j] == '>'){
							break;
						}
					}
					i=j;
					continue;
				}
				result_anc[re_ind] = temp_anc[i];
				re_ind ++;
			}
			
			ch_anc = String.valueOf(result_anc);
			
			return ch_anc;
		}
	};
}
