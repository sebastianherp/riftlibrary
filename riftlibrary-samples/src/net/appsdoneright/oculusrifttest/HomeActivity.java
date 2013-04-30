package net.appsdoneright.oculusrifttest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Iterator;

import net.appsdoneright.oculusrifttest.util.SystemUiHider;
import net.appsdoneright.riftlib.RiftConnection;
import net.appsdoneright.riftlib.util.RiftHandler;
import net.appsdoneright.riftlib.util.TrackerMessage;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class HomeActivity extends Activity {
	private static final String TAG = "HomeActivity";
	
	
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	
	private TextView mLogView;
	
	private RiftConnection mRift;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mRift = new RiftConnection(getApplicationContext());
		mRift.setRiftHandler(mRiftHandler);
		
		setContentView(R.layout.activity_home);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnTouchListener(
				mDelayHideTouchListener);
		
		
		
		mLogView = (TextView) findViewById(R.id.textViewLog);
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
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	

	
	public void handleClick(View v) {
		mRift.connect(null);
	}
	

	public static class MessageBodyFrame {
		double mAcceleration[] = new double[3];
		double mRotationRate[] = new double[3];
		double mMagneticField[] = new double[3];
		float mTemperature;
		float mTimeDelta;
	}
	
	
	
	private final RiftHandler mRiftHandler = new RiftHandler() {
		private int counter = 0;
		private boolean keepAliveResult = false;
		private TrackerMessage msg = new TrackerMessage();
		private static final float timeUnit = (1.0f / 1000.0f);
		
		@Override
		public void onDataReceived(byte[] buffer, final int length) {
			if(length == 62) {
				msg.parseBuffer(buffer);
				
				MessageBodyFrame sensors = new MessageBodyFrame();
				int iterations = msg.mSampleCount;
				if(msg.mSampleCount > 3) {
					iterations = 3;
					sensors.mTimeDelta = (msg.mSampleCount - 2) * timeUnit;
				} else {
					sensors.mTimeDelta = timeUnit;
				}
				
				for(int i=0; i < iterations; i++) {
					sensors.mAcceleration[0] = msg.samples[i].mAcc.x;
					sensors.mAcceleration[1] = msg.samples[i].mAcc.y;
					sensors.mAcceleration[2] = msg.samples[i].mAcc.z;
					sensors.mRotationRate[0] = msg.samples[i].mGyro.x;
					sensors.mRotationRate[1] = msg.samples[i].mGyro.y;
					sensors.mRotationRate[2] = msg.samples[i].mGyro.z;
					sensors.mMagneticField[0] = msg.mMag.x;
					sensors.mMagneticField[1] = msg.mMag.y;
					sensors.mMagneticField[2] = msg.mMag.z;
					
					sensors.mTemperature = msg.mTemperature;
					
					//updateOrientation(sensors);
					
					sensors.mTimeDelta = timeUnit;					
				}
				
			}
			
			
			
			counter++;
			if(counter % 100 == 0) {
				Log.i(TAG, counter + " packets");
				final String message = msg.toString();
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
	
						
						mLogView.setText(counter + ". bytes received: " + length + " (" + keepAliveResult + ")\n" + 
								message
						);
					}
				});
			}
			
		}

		@Override
		public void onKeepAlive(boolean result) {
			keepAliveResult = result;
		}
		
	};
}
