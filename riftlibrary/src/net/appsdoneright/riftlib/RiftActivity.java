package net.appsdoneright.riftlib;

import net.appsdoneright.riftlib.util.Quaternion;
import net.appsdoneright.riftlib.util.RiftHandler;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class RiftActivity extends Activity {
	private RiftConnection mRift;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRift = new RiftConnection(getApplicationContext());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mRift.connectIntent(getIntent());
	}

	@Override
	protected void onPause() {
		super.onPause();		
		mRift.disconnect();
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}	
	
	
	protected void connectRift() {
		mRift.connect(null);
	}
	
	protected void setRiftHandler(RiftHandler handler) {
		mRift.setRiftHandler(handler);
	}

}
