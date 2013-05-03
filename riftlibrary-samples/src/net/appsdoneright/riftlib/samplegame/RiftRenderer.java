package net.appsdoneright.riftlib.samplegame;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.appsdoneright.riftlib.util.Quaternion;
import net.appsdoneright.riftlib.util.RiftHandler;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class RiftRenderer implements Renderer, RiftHandler {
    private static final String TAG = RiftRenderer.class.getSimpleName();
    private static final boolean D = true;
    
    private static final float SIZE_WORLD = 40f; // in meter
    private static final float PLAYER_WIDTH = 1f; // distance to wall where movement should stop
    
    private Shapes mFloor;
    private Shapes mCube;
    private Shapes mCube2;
    
    private RiftCamera mCamera;
    
    private int mHalfWidth, mHeight;
    private float mRatio;

    // Declare as volatile because we are updating it from another thread
    public volatile float mdFOV = 0f;
    public volatile float mdAngle = 0f;
    public volatile float mdPosX = 0;
    public volatile float mdPosY = 0;
    public volatile float mIPD = 1.0f;
    
    private volatile Quaternion mQuaternion = new Quaternion();
    
    private int frameCounter = 0;
    private long frameCheckTime = 0;
    
    private Context mContext;
    
    public RiftRenderer(Context context) {
    	mContext = context;
    }
    
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        mFloor = Shapes.Floor(mContext, 1.0f).scale(SIZE_WORLD, 0.1f, SIZE_WORLD).translate(0f, -1f, 0f);
        
        mCube = Shapes.ColorCube(mContext, 1.0f).rotate(-40, 1, -1, 0).translate(0, 1.0f, 0);
        mCube2 = Shapes.ColorCube(mContext, 2.0f).translate(-3f, 1.0f, 0.0f);
        
        mCamera = new RiftCamera(RiftCamera.PLAYER_IPD, RiftCamera.PLAYER_EYE_HEIGHT, RiftCamera.CAMERA_FOV, 1);
        mCamera.mPosZ = 10;
        mIPD = mCamera.getIPD();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
    	
        movePlayer();

        updateScene();
        
        renderFrame();

        
    	if(frameCheckTime < System.currentTimeMillis()) {
    		if(D) Log.d(TAG, String.format("FPS: %d, angle: %.2f, x: %.2f, y: %.2f, IPD: %.4f, FOV: %.2f",
    						frameCounter, mCamera.mYaw, mCamera.mPosZ, mCamera.mPosX, mCamera.getIPD(), mCamera.getFOV()) );
    		frameCounter = 0;
    		frameCheckTime += 1000;
    	}
    	frameCounter++;
    }
    
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
    	
    	mHalfWidth = width/2;
    	mHeight = height;
    	
        mRatio = (float) mHalfWidth / height;

        mCamera.setFOV(mCamera.getFOV(), mRatio);
        
        frameCheckTime = System.currentTimeMillis() + 1000;;
    }

	@Override
	public void onDataReceived(Quaternion q, int frequency) {
		mQuaternion.set(q);
	}

	@Override
	public void onKeepAlive(boolean result) {
		
	}
    
    private void movePlayer() {
    	mCamera.mYaw += mdAngle;
    	float cosAngle = (float)Math.cos(mCamera.mYaw / 180.0 * Math.PI);
    	float singAngle = (float)Math.sin(mCamera.mYaw / 180.0 * Math.PI);
    	
        mCamera.mPosZ += cosAngle * mdPosX + singAngle * mdPosY;
        mCamera.mPosX += cosAngle * mdPosY - singAngle * mdPosX;
        mCamera.setIPD(mIPD);
        mCamera.setHeadOrientation(mQuaternion);

        if(mdFOV != 0)
        	mCamera.setFOV(mCamera.getFOV()+mdFOV, mRatio);

        
        mdAngle = 0;
        mdPosX = 0;
        mdPosY = 0;
        mdFOV = 0;
        
    	// collision with room walls?
    	float border = SIZE_WORLD/2 - PLAYER_WIDTH;
    	mCamera.mPosZ = Math.min(border, Math.max(-border, mCamera.mPosZ));
    	mCamera.mPosX = Math.min(border, Math.max(-border, mCamera.mPosX));


        
        mCamera.update(); 
    }
    
    private void updateScene() {
    	mCube.rotate(1.5f, 6f, 2.7f, 3.5f);
    }
    
    private int mTextureWidth, mTextureHeight;
    int[] fb, depthRb, renderTex;
    
    private boolean renderFrameToTexture() {
    	
    	GLES20.glViewport(0, 0, mTextureWidth, mTextureHeight);
    	
    	GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fb[0]);
    	
    	GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, renderTex[0], 0);
    	
    	GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRb[0]);
    	
    	int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
		if (status != GLES20.GL_FRAMEBUFFER_COMPLETE)
			return false;
		
		renderFrame();
		
		GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
		
		GLES20.glClearColor(0f, 0f, 0f, 1f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		return true;
    }
    
    private void renderFrame() {
    	// clear background
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        // view for left eye
       	GLES20.glViewport(0, 0, mHalfWidth, mHeight);
        // draw scene
       	drawScene(mCamera.mVMatrixLeft, mCamera.mProjMatrix);
        // flush
        GLES20.glFlush();
        
        
        // view for right eye
       	GLES20.glViewport(mHalfWidth, 0, mHalfWidth, mHeight);
    	// draw scene
       	drawScene(mCamera.mVMatrixRight, mCamera.mProjMatrix);
        // flush
        GLES20.glFlush();
    }

    private void drawScene(float[] VMatrix, float[] PMatrix) {
    	GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    	mFloor.draw(VMatrix, PMatrix);
    	
    	GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    	GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        
        mCube.draw(VMatrix, PMatrix);
        mCube2.draw(VMatrix, PMatrix);
    } 

}