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

public class FindDest {
	private HttpClient httpclient;
	
	private SearchDest searchDest;
	
	final static public String TAG_CLIENT = "client";
//	final static public String TAG_SERVER = "server";
	
	private String dest;
	private LatLng loc;
	
	private List<SearchMarkers> positions;
	
	public FindDest(String d, LatLng l){
		dest = d;
		loc = l;
	}
	
	//1. call API url and get JSON
	public List<SearchMarkers> reqDest(){
		searchDest = new SearchDest(dest);
		searchDest.start();
		while(searchDest.isAlive()){}
		System.out.println("THREAD IS ALL DONE");
		return positions;
	}
	
	private class SearchDest extends Thread {
		private String dest;
		public SearchDest(String d){
			//change string to parameter form
			dest = changePar(d);
		}
		
		public URI makeParameter(LatLng loc){
			URI uri = null;
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			String locInfo = loc.latitude+","+loc.longitude;
			params.add(new BasicNameValuePair("near", locInfo));
		    params.add(new BasicNameValuePair("q", dest));
		    params.add(new BasicNameValuePair("output", "json"));
		    params.add(new BasicNameValuePair("radius", "10")); //마일로 표시된 반경
//		    params.add(new BasicNameValuePair("mrt", "yp")); //검색오션. 업체
		    params.add(new BasicNameValuePair("num", "5"));
		    
		    try {
//		    	http://maps.google.com/?near=40.4245695%2C-86.9127475&q=knoy%2Bhall&output=json&radius=3&num=5
				uri = URIUtils.createURI("http", "maps.google.com", -1, null, URLEncodedUtils
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
				URI sendURI = makeParameter(loc);
				
				//https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=40.4279243,-86.9111332=&radius=500&name=union&sensor=true&key=AIzaSyBv-2PHlGQ_rd32A19uSrrCATUGF8msRlU
				get.setURI(sendURI);
//				get.setURI(new URI("https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
//						"location="+ loc.latitude+ "," + loc.longitude + "&radius=500" +
//						"&name="+par+"&sensor=true&key=AIzaSyBv-2PHlGQ_rd32A19uSrrCATUGF8msRlU"));
//				
				
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
		
		private String changePar(String bef){
			int befLen = bef.length();
			String aft;
			
			char [] tempBef = bef.toCharArray();
			char [] tempAft = new char[befLen];
			int i, j;
			
			for(i=0; i<befLen; i++){
				if(tempBef[i]=='\n')
					continue;
				for(j=i; j<befLen && tempBef[j]!=' ' && tempBef[j]!='\n' ; j++){
					tempAft[j] = tempBef[j];
				}
				if(j<befLen){
					if(tempBef[j]==' ')
						tempAft[j] = '+';
				}
				i=j;	
			}
			aft = String.valueOf(tempAft);
			return aft;
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
			
			String jsonString = sb.toString().substring(9);
			positions = parseJSON(jsonString);
//			showMarkers(positions);
			return null;
		}
		
		public List<SearchMarkers> parseJSON(String json){
			List<SearchMarkers> result = null;
			try{
				JSONObject object = new JSONObject(json);
				JSONObject overlays = object.getJSONObject("overlays");
				JSONArray markers = overlays.getJSONArray("markers");
				
				if (markers != null) {
					int len = markers.length();
					result = new ArrayList<SearchMarkers>();
					String lat, lng;
					String addr;
					for (int i = 0; i < len; i++) {
						
						addr = markers.getJSONObject(i).getString("laddr");
						lat = markers.getJSONObject(i).getJSONObject("latlng")
								.getString("lat");
						lng = markers.getJSONObject(i).getJSONObject("latlng")
								.getString("lng");
						
						SearchMarkers sm = new SearchMarkers(addr, lat, lng);
						result.add(sm);
						System.out.println("addr: " + addr);
						System.out.println("latitude: " + lat);
						System.out.println("longitude: " + lng);
					}
	
	//				Message message = resultHandler.obtainMessage();
	//				Bundle bundle = new Bundle();
	//				bundle.putString(RESULT, SUCCESS_RESULT);
	//				bundle.putStringArrayList("searchList", searchList);
	//				message.setData(bundle);
	//				resultHandler.sendMessage(message);
				} else {
	//				Message message = resultHandler.obtainMessage();
	//				Bundle bundle = new Bundle();
	//				bundle.putString(RESULT, FAIL_MAP_RESULT);
	//				message.setData(bundle);
	//				resultHandler.sendMessage(message);
	//
	//				stringData = "JSon >> \n" + sb.toString();
//					result = null;
				}
			}catch (JSONException e){
				e.printStackTrace();
			}
			return result;
		}
	};
	
}
