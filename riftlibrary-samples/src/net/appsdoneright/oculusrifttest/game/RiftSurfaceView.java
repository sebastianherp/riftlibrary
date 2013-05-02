package net.appsdoneright.oculusrifttest.game;

import net.appsdoneright.riftlib.util.Quaternion;
import net.appsdoneright.riftlib.util.RiftHandler;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class RiftSurfaceView extends GLSurfaceView implements RiftHandler {

	private final RiftRenderer mRenderer;
	
	public RiftSurfaceView(Context context) {
		super(context);

		setEGLContextClientVersion(2);
		
		mRenderer = new RiftRenderer();
		setRenderer(mRenderer);
		
		this.setKeepScreenOn(true);
		
		// Render the view only when there is a change in the drawing data
		//setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
                //requestRender();
                
                if (x < getWidth() / 2) {
                	mRenderer.mPosY += (dx / 50f);
                	mRenderer.mPosX += (dy / 50f);
                } else {
                	if (y < getHeight() / 2) {
                		mRenderer.mIPD += (dx / 50f);
                	}
                }
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

	@Override
	public void onDataReceived(Quaternion q, int frequency) {
		
		mRenderer.mQuaternion = q;
	}

	@Override
	public void onKeepAlive(boolean result) {
	}

}
