package net.appsdoneright.riftlib.samplegame;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import net.appsdoneright.oculusrifttest.R;
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_reset_view:
			Toast.makeText(this, getResources().getString(R.string.function_not_implemented), Toast.LENGTH_SHORT).show();
			return true;
		
		case R.id.menu_enable_fov_ipd:
			item.setChecked(!item.isChecked());
			mGLView.setFOVIPDenabled(item.isChecked());
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
