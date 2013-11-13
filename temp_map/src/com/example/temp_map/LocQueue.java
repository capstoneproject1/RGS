package com.example.temp_map;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;

public class LocQueue<T> extends AbstractQueue<T>{

	private LinkedList<T> myqueue = null;
	
	public LocQueue(){
		myqueue = new LinkedList<T>();
	}
	
	//List Option for getting in i index
	public T get(int i){
		return myqueue.get(i);
	}
	
	//Stack Option for popping upper one
	public T pop(){
		if(myqueue.size()>0)
			return myqueue.removeLast();
		return null;
	}
	
	@Override
	public boolean offer(T e) {
		// TODO Auto-generated method stub
		return myqueue.offer(e);
	}

	@Override
	public T peek() {
		// TODO Auto-generated method stub
		return myqueue.peek();
	}

	@Override
	public T poll() {
		// TODO Auto-generated method stub
		return myqueue.poll();
	}

	@Override
	public Iterator<T> iterator() {
		// TODO Auto-generated method stub
		return myqueue.iterator();
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return myqueue.size();
	}

}
