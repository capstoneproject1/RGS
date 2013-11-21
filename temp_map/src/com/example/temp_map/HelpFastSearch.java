package com.example.temp_map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import android.os.Environment;
import android.util.Log;

public class HelpFastSearch {
	
	File file;
	
	public HelpFastSearch(String fileName){
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File (sdCard.getAbsolutePath()+"/temp_map");
		if(!dir.exists())
			dir.mkdirs();
		Log.e("Making Directories", String.valueOf(dir.exists()));
		file = new File(dir, fileName);
	}
	
	public void testFS(FastSearch fs){
		LocQueue<SearchMarkers> myRecent = fs.getMyRecent();
		List<RankMarkers> myRank = fs.getMyRank();
		
		System.out.println(fs.getMyHome());
		System.out.println(fs.getMyWork());
		
		int rec_len = myRecent.size();
		int rnk_len = myRank.size();
		
		for(int i=0; i<rec_len; i++)
			Log.d(String.valueOf(i), myRecent.get(i).getAddr());
		for(int i=0; i<rnk_len; i++)
			Log.e(String.valueOf(i), myRank.get(i).getAddr());
	}
	
	public boolean deleteFS(){
		return file.delete();
	}
	
	public FastSearch loadFS(){
		FastSearch fs;
		try{
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			fs = (FastSearch) ois.readObject();
			ois.close();
			return fs;
		} catch (Exception e){
			e.printStackTrace();
			fs = new FastSearch();
			saveFS(fs);
			return fs;
		}
	}
	
	public void saveFS(FastSearch fs){
		try{
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(fs);
			oos.flush();
			oos.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
}
