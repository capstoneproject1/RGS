package com.example.temp_map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class LocQueue<T> extends AbstractQueue<T> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private LinkedList<T> myqueue = null;
	
	private void writeObject(ObjectOutputStream stream)
            throws IOException {
        stream.writeObject(myqueue);
    }

    @SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        myqueue = (LinkedList<T>) stream.readObject();
    }
    
	public LocQueue(){
		myqueue = new LinkedList<T>();
	}
	
	//List Option for getting in i index
	public T get(int i){
		return myqueue.get(i);
	}
	
	//Delete in index position
	public T delete(int i){
		return myqueue.remove(i);
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
