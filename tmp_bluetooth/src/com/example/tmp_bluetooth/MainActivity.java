package com.example.tmp_bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity {
	 final int REQUEST_ENABLE_BT = 30; // bluetooth variable enable or disable
	 
	 public BroadcastReceiver mReceiver;
	 
//	 ArrayList<String> temp_als = new ArrayList<String>();
	 ArrayAdapter<String> mArrayAdapter = null;
	 private ListView lv = null;
	 List<BluetoothDevice> bi_list = new ArrayList<BluetoothDevice>();
	 Button rb;
	 BluetoothAdapter mBluetoothAdapter;
	 
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  setContentView(R.layout.activity_main); 
		  
		  rb = (Button)findViewById(R.id.rb);
		  
		  lv = (ListView)findViewById(R.id.listView1);
		  mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		  mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		 // 1.Get the BluetoothAdapter 
		 if (mBluetoothAdapter == null) {
		     // Device does not support Bluetooth
			 System.out.println("You can't use bluetooth~!");
		 }
		  
		 // 2.Enable Bluetooth 
		 if (!mBluetoothAdapter.isEnabled()) {
		     Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		     startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		 }
		 
		 Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		 discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
		 startActivity(discoverableIntent);
		 
		 bluetooth_list();
		 
		 rb.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
//            	 System.out.println("button click");
//            	 Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//    			 discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
//    			 startActivity(discoverableIntent);
            	 mBluetoothAdapter.startDiscovery();
             }
         });
		 
	}

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	System.out.println("c'mon");
		if(resultCode == 0){
			System.out.println("Reject!");
		}else if(resultCode == -1){
	        if (requestCode == REQUEST_ENABLE_BT) {
	            // code to handle cancelled state
	        	bluetooth_list();
	        }
		}
    }
	 
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
	
	public void bluetooth_list(){
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		 // If there are paired devices
		 if (pairedDevices.size() > 0) {
		     // Loop through paired devices
		     for (BluetoothDevice device : pairedDevices) {
		    	 bi_list.add(device);
		         // Add the name and address to an array adapter to show in a ListView
		         mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		     }
		 }
		 lv.setAdapter(mArrayAdapter);
		 
		 lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		      @Override
		      public void onItemClick(AdapterView<?> parent, final View view,
		          int position, long id) {
			        System.out.println(bi_list.get(position).getName());
			        AcceptThread at = new AcceptThread(bi_list.get(position).getName(), 
			        		bi_list.get(position).getUuids());
			        at.start();
		      }

		 });
		 
		// Create a BroadcastReceiver for ACTION_FOUND
		mReceiver = new BroadcastReceiver() {
		     public void onReceive(Context context, Intent intent) {
		         String action = intent.getAction();
		         
		         // When discovery finds a device
		         if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		             // Get the BluetoothDevice object from the Intent
		             BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		             // Add the name and address to an array adapter to show in a ListView
		             bi_list.add(device);
			         // Add the name and address to an array adapter to show in a ListView
			         mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		         }
		     }
		 };
		 // Register the BroadcastReceiver
		 IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		 registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
		 
		 if (mBluetoothAdapter.isDiscovering()) {
			 mBluetoothAdapter.cancelDiscovery();
		 }
		 
		 mBluetoothAdapter.startDiscovery();
		 
	}
	
	private class AcceptThread extends Thread {
	    private final BluetoothServerSocket mmServerSocket;
	 
	    public AcceptThread(String name, ParcelUuid[] parcelUuids) {
	        // Use a temporary object that is later assigned to mmServerSocket,
	        // because mmServerSocket is final
	        BluetoothServerSocket tmp = null;
	        try {
	            // MY_UUID is the app's UUID string, also used by the client code
	        	int parlen = parcelUuids.length;
	        	System.out.println(parlen);
//	        	if(parlen > 0){
//	        		for(int i=0; i<parlen; i++){
		        		UUID uuid = parcelUuids[0].getUuid();
		        		System.out.println(uuid.toString());
//		    		}
//	        	}
	        	tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(name, uuid);
	        } catch (IOException e) { 
	        	e.printStackTrace();
	        }
	        mmServerSocket = tmp;
	    }
	 
	    public void run() {
	        BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	        while (true) {
	            try {
	                socket = mmServerSocket.accept();
	            } catch (IOException e) {
	                break;
	            }
	            // If a connection was accepted
	            if (socket != null) {
	                // Do work to manage the connection (in a separate thread)
	                manageConnectedSocket(socket);
	            	System.out.println("gogo");
	            	try {
						mmServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                break;
	            }
	        }
	    }
	 
	    /** Will cancel the listening socket, and cause the thread to finish */
	    public void cancel() {
	        try {
	            mmServerSocket.close();
	        } catch (IOException e) { }
	    }
	    
	    public void manageConnectedSocket(BluetoothSocket socket){
	    	System.out.println("good!");
//	    	ConnectedThread ct = new ConnectedThread(socket);
//	    	ct.start();
	    }
	    
	    private class ConnectedThread extends Thread {
	        private final BluetoothSocket mmSocket;
	        private final InputStream mmInStream;
	        private final OutputStream mmOutStream;
	     
	        public ConnectedThread(BluetoothSocket socket) {
	            mmSocket = socket;
	            InputStream tmpIn = null;
	            OutputStream tmpOut = null;
	     
	            // Get the input and output streams, using temp objects because
	            // member streams are final
	            try {
	                tmpIn = socket.getInputStream();
	                tmpOut = socket.getOutputStream();
	            } catch (IOException e) { }
	     
	            mmInStream = tmpIn;
	            mmOutStream = tmpOut;
	        }
	     
	        public void run() {
	            byte[] buffer = new byte[1024];  // buffer store for the stream
	            int bytes; // bytes returned from read()
	     
	            // Keep listening to the InputStream until an exception occurs
	            while (true) {
	                try {
	                    // Read from the InputStream
	                    bytes = mmInStream.read(buffer);
	                    // Send the obtained bytes to the UI activity
//	                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//	                            .sendToTarget();
	                } catch (IOException e) {
	                    break;
	                }
	            }
	        }
	     
	        /* Call this from the main activity to send data to the remote device */
	        public void write(byte[] bytes) {
	            try {
	                mmOutStream.write(bytes);
	            } catch (IOException e) { }
	        }
	     
	        /* Call this from the main activity to shutdown the connection */
	        public void cancel() {
	            try {
	                mmSocket.close();
	            } catch (IOException e) { }
	        }
	    }
	}
	
	
}
