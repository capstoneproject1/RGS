package com.example.temp_map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

//Recent Log & Usually Searching Log & User Saved Log Information
public class FastSearch implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private SearchMarkers myHome = null;
	private SearchMarkers myWork = null;
	private LocQueue<SearchMarkers> myRecent;
	private List<RankMarkers> myRank;
	
	public FastSearch(){
		myRecent = new LocQueue<SearchMarkers>();
		myRank = new ArrayList<RankMarkers>();
		
	}
	
	private void writeObject(ObjectOutputStream stream)
            throws IOException {
        stream.writeObject(myHome);
        stream.writeObject(myWork);
        stream.writeObject(myRecent);
        stream.writeObject(myRank);
    }

    @SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        myHome = (SearchMarkers) stream.readObject();
        myWork = (SearchMarkers) stream.readObject();
        myRecent = (LocQueue<SearchMarkers>) stream.readObject();
        myRank = (List<RankMarkers>) stream.readObject();
    }
	
	//When destination is searched, this function is always called.
	public void searchExec(SearchMarkers sm){
		setMyRank(sm);
		setMyRecent(sm);
	}
	
	private void setMyRank(SearchMarkers sm){
		//find older one
		int myRanklen = myRank.size();
		boolean breaking = false;
		
		if(myRanklen == 0){
			RankMarkers temp_rm = new RankMarkers(sm);
			myRank.add(temp_rm);
		}else{
			for(int i=0; i<myRanklen; i++){
				//Already Exist
				if(myRank.get(i).getAddr().equals(sm.getAddr())){
					myRank.get(i).call();
					changeRank(i, myRanklen, myRank.get(i).getCallCount());
					breaking = true;
					break;
				}
			}
			
			if(breaking == false){
				RankMarkers temp_rm = new RankMarkers(sm);
				myRank.add(temp_rm);
			}
		}
		
	}
	
	//0: highest -> size(): lowest
	private void changeRank(int ind, int len, int cnt){
		boolean breaking = false;
		if(ind <= 0){
			return;
		}else{
			RankMarkers move_rm = myRank.get(ind);
			
			for(int i=ind-1; i>=0; i--){
				RankMarkers temp_rm = myRank.get(i);
				if(temp_rm.getCallCount() < cnt){
					myRank.set(i+1, temp_rm);
				}else{
					myRank.set(i+1, move_rm);
					breaking = true;
					break;
				}
			}
			
			if(breaking == false)
				myRank.set(0, move_rm);
		}
	}
	
	//Only save 5 recent position
	private void setMyRecent(SearchMarkers sm){
		
		int rec_len = myRecent.size();
		if(rec_len == 0){
			myRecent.add(sm);
		}else{
			if(!findSame(rec_len, sm)){
				myRecent.add(sm);
				if(myRecent.size() > 5){
					myRecent.remove();
				}
			}
		}
	}
	
	//Checking same one in recent list
	private boolean findSame(int len, SearchMarkers nowsm){
		for(int i=0; i<len; i++){
			SearchMarkers tempsm = myRecent.get(i);
			
			//find same
			if(tempsm.getAddr().equals(nowsm.getAddr())){
				myRecent.delete(i);
				myRecent.add(nowsm);
				return true;
			}
		}
		return false;
	}
	
	public LocQueue<SearchMarkers> getMyRecent() {
		return myRecent;
	}

	public List<RankMarkers> getMyRank() {
		return myRank;
	}

	public SearchMarkers getMyHome() {
		return myHome;
	}
	public void setMyHome(SearchMarkers myHome) {
		this.myHome = myHome;
	}
	public SearchMarkers getMyWork() {
		return myWork;
	}
	public void setMyWork(SearchMarkers myWork) {
		this.myWork = myWork;
	}
	
}
