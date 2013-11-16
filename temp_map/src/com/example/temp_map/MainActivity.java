package com.example.temp_map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	TextView destText;
	
	ExpandableListView elv;
	List<String> listHeader;
	HashMap<String, List<String>> listChild;
	ExpandableListAdapter listAdapter;
	
	private final int NEWSEARCH = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_dest);
		Button destBtn = (Button) findViewById(R.id.destButton);
		
		elv = (ExpandableListView) findViewById(R.id.expandableListView1);
		setAllList();
		listAdapter = new ExpandableListAdapter(this, listHeader, listChild);
		elv.setAdapter(listAdapter);
		
		elv.setOnChildClickListener(new OnChildClickListener() {
		 
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                    int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(MainActivity.this, ShowMap.class);
                
                Log.e("G",String.valueOf(groupPosition));
                Log.e("C",String.valueOf(childPosition));
                
                String item = listChild.get(listHeader.get(groupPosition)).get(childPosition);
                
                if(!item.equals("Not Registered")){
	        		intent.putExtra("dest", item);
	        		intent.putExtra("group_option", groupPosition);
	        		intent.putExtra("child_option", childPosition);
	                startActivity(intent);
            	}
                return false;
            }
        });
		 
        destText = (TextView) findViewById(R.id.destText);
        destBtn.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		String destination = destText.getText().toString();
        		Intent intent = new Intent(MainActivity.this, ShowMap.class);
        		intent.putExtra("dest", destination);
        		intent.putExtra("group_option", NEWSEARCH);
        		intent.putExtra("child_option", NEWSEARCH);
        		startActivity(intent);
        	}
        });
	}

	//Setting in ExpandableList
	public void setAllList(){
		listHeader = new ArrayList<String>();
		listChild = new HashMap<String, List<String>>();
		
		listHeader.add("Home");
		listHeader.add("Work");
		listHeader.add("Recent");
		listHeader.add("Rank");
		
		//Load from SDCard
        String fileName = "file.txt";
		HelpFastSearch hfs = new HelpFastSearch(fileName);
//		hfs.deleteFS();
		FastSearch fs = hfs.loadFS();
		
		List<String> home = new ArrayList<String>();
		SearchMarkers mh = fs.getMyHome();
		if(mh != null)
			home.add(mh.getAddr());
		else
			home.add("Not Registered");
		
		List<String> work = new ArrayList<String>();
		SearchMarkers mw = fs.getMyHome();
		if(mw != null)
			work.add(mw.getAddr());
		else
			work.add("Not Registered");
		
		List<String> recent = new ArrayList<String>();
		LocQueue<SearchMarkers> mr = fs.getMyRecent();
		int rec_len = mr.size();
		if(rec_len > 0){
			for(int i=rec_len-1; i>=0; i--){
				SearchMarkers temp_rec = mr.get(i);
				recent.add(temp_rec.getAddr());
			}
		}else
			recent.add("Not Registered");
		
		List<String> rank = new ArrayList<String>();
		List<RankMarkers> mr2 = fs.getMyRank();
		int rank_len = mr2.size();
		if(rank_len > 0){
			for(int i=0; i<5 && i<rank_len; i++){
				RankMarkers temp_rank = mr2.get(i);
				rank.add(temp_rank.getAddr());
			}
		}else
			rank.add("Not Registered");
		
		listChild.put(listHeader.get(0), home);
		listChild.put(listHeader.get(1), work);
		listChild.put(listHeader.get(2), recent);
		listChild.put(listHeader.get(3), rank);
	}
}