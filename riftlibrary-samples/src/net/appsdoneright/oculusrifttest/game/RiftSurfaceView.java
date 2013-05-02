package net.appsdoneright.oculusrifttest.game;

import net.appsdoneright.riftlib.util.Quaternion;
import net.appsdoneright.riftlib.util.RiftHandler;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;

public class RiftSurfaceView extends GLSurfaceView implements RiftHandler {

	private final RiftRenderer mRenderer;
    private final float SCREEN_DENSITY;
	
	public RiftSurfaceView(Context context) {
		super(context);

		setEGLContextClientVersion(2);
		
		mRenderer = new RiftRenderer();
		setRenderer(mRenderer);
		
		this.setKeepScreenOn(true);
		
		SCREEN_DENSITY = context.getResources().getDisplayMetrics().density;
		
		// Render the view only when there is a change in the drawing data
		//setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}
	
    private float mPreviousX1;
    private float mPreviousY1;
    private float mPreviousX2;
    private float mPreviousY2;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

    	int action = ev.getAction() & MotionEvent.ACTION_MASK;
        int pointerId;
    	
        float x1 = -1;
        float y1 = -1;
        float x2 = -1;
        float y2 = -1;
        
        for(int i=0; i < ev.getPointerCount(); i++) {
        	pointerId = ev.getPointerId(i);
        	if(pointerId == 0) {
        		x1 = ev.getX(i);
        		y1 = ev.getY(i);
        	}
        	if(pointerId == 1) {
        		x2 = ev.getX(i);
        		y2 = ev.getY(i);
        	}
        }
        
        PointerCoords first = new PointerCoords();
        PointerCoords second = new PointerCoords();

        switch (action) {
            case MotionEvent.ACTION_MOVE:

                float dx, dy;

                for(int i=0; i < ev.getPointerCount(); i++) {
                	pointerId = ev.getPointerId(i);
                	if(pointerId == 0) {
                		dx = x1 - mPreviousX1;
                		dy = y1 - mPreviousY1;
                		if (x1 < getWidth() / 2) {
                        	// first pointer in left half
                        	mRenderer.mdPosY += (dx / 100f * SCREEN_DENSITY);
                        	mRenderer.mdPosX += (dy / 100f * SCREEN_DENSITY);
                		} else {
                			// first pointer in right half
                        	if (y1 < getHeight() / 2) {
                        		mRenderer.mIPD += (dx / 100f * SCREEN_DENSITY);
                        	} else {
                        		mRenderer.mdAngle += (dx / 20f * SCREEN_DENSITY);
                        	}
                		}
                	}
                	if(pointerId == 1) {
                		dx = x2 - mPreviousX2;
                		dy = y2 - mPreviousY2;
                		if (x2 < getWidth() / 2) {
                        	// first pointer in left half
                        	mRenderer.mdPosY += (dx / 100f * SCREEN_DENSITY);
                        	mRenderer.mdPosX += (dy / 100f * SCREEN_DENSITY);
                		} else {
                			// first pointer in right half
                        	if (y2 < getHeight() / 2) {
                        		mRenderer.mIPD += (dx / 100f * SCREEN_DENSITY);
                        	} else {
                        		mRenderer.mdAngle += (dx / 20f * SCREEN_DENSITY);
                        	}
                		}
                	}
                }
        }

        mPreviousX1 = x1;
        mPreviousY1 = y1;
        mPreviousX2 = x2;
        mPreviousY2 = y2;
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
