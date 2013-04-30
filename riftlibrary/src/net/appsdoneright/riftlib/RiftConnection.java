package net.appsdoneright.riftlib;

import java.util.HashMap;
import java.util.Iterator;

import net.appsdoneright.riftlib.util.RiftHandler;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class RiftConnection {
	private static final String TAG = RiftConnection.class.getSimpleName();
	private static final boolean D = true;
	
	private static final int RIFT_VENDOR_ID = 10291;
	private static final int RIFT_PRODUCT_ID = 1;
	private static final String ACTION_USB_PERMISSION = "net.appsdoneright.oculusrifttest.USB_PERMISSION";
	
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;

	private RiftRunnable mUsbLoop;
	private Thread mUsbThread;
	
	private static final Object[] lock = new Object[]{};
	private boolean mStopThread = false;
	
	private RiftHandler mRiftHandler = null;
	private Context mContext;
	
	public RiftConnection(Context context) {
		mContext = context;
		
		mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
	}
	
	public void setRiftHandler(RiftHandler handler) {
		mRiftHandler = handler;
	}
	
	public void connectIntent(Intent intent) {
		UsbDevice device = null;
		String action = intent.getAction();
		if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
			device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
		}
		connect(device);
	}
	
	public void connect(UsbDevice device) {
		if(D) Log.d(TAG, "Connect to Rift");
		
		if(device == null) {
			HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
			Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
			while(deviceIterator.hasNext()){
			    UsbDevice tmpDevice = deviceIterator.next();
			    
			    if(tmpDevice.getVendorId() == RIFT_VENDOR_ID && tmpDevice.getProductId() == RIFT_PRODUCT_ID) {
			    	device = tmpDevice;
			    }
			    
			}
		}
		
		if(device != null) {
			mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
			mContext.registerReceiver(mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
			mContext.registerReceiver(mUsbReceiver, new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED));
			
			if(D) Log.d(TAG, "Requesting permission");
			mUsbManager.requestPermission(device, mPermissionIntent);
		} else {
			if(D) Log.d(TAG, "Rift not connected");
		}
	}
	
	public void disconnect() {
		stopCommunication();
		try {
			mContext.unregisterReceiver(mUsbReceiver);
		} catch (Exception e) {
			// ignore
		}
	}
	
	private void startCommunication(UsbDevice device) {
		
		if(mUsbLoop != null) {
			Log.d(TAG, "Thread still running and still connected to Rift");
			return;
		}

		mUsbLoop = new RiftRunnable(device);

		Log.d(TAG, "Start communication");
		
		mUsbThread = new Thread(mUsbLoop);
		mUsbThread.start();
		
	}
	
	private void stopCommunication() {
		Log.d(TAG, "Stop communication");
		
		mStopThread = true;
		try {
			if(mUsbThread != null)
				mUsbThread.join();
		} catch(InterruptedException ex) {
			// ignore
		}
		mStopThread = false;
		mUsbLoop = null;
		mUsbThread = null;
		
	}
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				// detached -> disconnect
				synchronized (this) {
					UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					stopCommunication();
				}
			} else if(ACTION_USB_PERMISSION.equals(action)) {
				// permission granted?
				synchronized (this) {
					UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if(device != null) {
							// connect
							startCommunication(device);
						}
					} else {
						Log.e(TAG, "Permission to connect to Rift was denied");
					}
				}
			}
			
		}
	};
	
	private static boolean sendKeepAlive(int keepAliveInterval, UsbDeviceConnection connection) {
		byte[] buf = new byte[4];
		
		int command = 0;
		
		buf[0] = (byte) (command & 0xFF);
		buf[0] = (byte) (command >> 8);
		buf[0] = (byte) (keepAliveInterval & 0xFF);
		buf[0] = (byte) (keepAliveInterval >> 8);
		
		
		// http://www.usb.org/developers/devclass_docs/HID1_11.pdf from page 51
		// 0x21   => Send direction
		// 0x09   => Set_Report request
		// 0x0308 => Report Type Feature 0x03 << 8 | Report ID 0x08 (keep alive)
		int len = connection.controlTransfer(0x21, 0x09, 0x0308, 0, buf, 4, 0);
		return len >= 0;		
	}
	
	private class RiftRunnable implements Runnable {
		private final UsbDevice mDevice;
		private long nextKeepAlive = 0;

		RiftRunnable(UsbDevice device) {
			mDevice = device;
		}

		@Override
		public void run() {
			UsbDeviceConnection mConnection = mUsbManager.openDevice(mDevice);
			UsbInterface mUsbInterface = mDevice.getInterface(0);
			
			if(!mConnection.claimInterface(mUsbInterface, true))
				return;

			sendKeepAlive(10000, mConnection);
			nextKeepAlive = System.currentTimeMillis() + 3000; // 3 seconds interval
			
			UsbEndpoint mEndpointIN = mUsbInterface.getEndpoint(0); // endpoint 0 on interface 0 is the only one
			int bufferSize = mEndpointIN.getMaxPacketSize();
			byte buffer[] = new byte[256];

			for(;;) { // the loop
				int receivedBytes = mConnection.bulkTransfer(mEndpointIN, buffer, bufferSize, 100);
				if(receivedBytes > 0 && mRiftHandler != null)
					mRiftHandler.onDataReceived(buffer, receivedBytes);
				
				long now = System.currentTimeMillis();
				if(nextKeepAlive < now) {
					boolean res = sendKeepAlive(10000, mConnection);
					if(mRiftHandler != null)
						mRiftHandler.onKeepAlive(res);
					nextKeepAlive = now + 3000;
				}
				
				if(mStopThread) {
					mConnection.releaseInterface(mUsbInterface);
					mConnection.close();
					break;
				}
			}
		}
	}
}
