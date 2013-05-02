package net.appsdoneright.oculusrifttest.game;

import android.os.Bundle;
import net.appsdoneright.riftlib.RiftActivity;

public class GameActivity extends RiftActivity {

	private RiftSurfaceView mGLView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGLView = new RiftSurfaceView(this);
		setRiftHandler(mGLView);
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
