package net.appsdoneright.riftlib.samplegame;

import android.os.Bundle;
import net.appsdoneright.riftlib.RiftActivity;

public class GameActivity extends RiftActivity {

	private RiftSurfaceView mGLView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGLView = new RiftSurfaceView(this);
		setRiftHandler(mGLView.getRenderer());
		setContentView(mGLView);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mGLView.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mGLView.onResume();
	}
	
}
