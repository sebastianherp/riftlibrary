package net.appsdoneright.riftlib.samplegame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.appsdoneright.riftlib.util.Quaternion;
import net.appsdoneright.riftlib.util.RiftHandler;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;
import android.os.SystemClock;
import android.util.Log;

public class RiftRenderer implements Renderer, RiftHandler {
    private static final String TAG = RiftRenderer.class.getSimpleName();
    private static final boolean D = true;
    
    private static final float SIZE_WORLD = 40f; // in meter
    private static final float PLAYER_WIDTH = 1f; // distance to wall where movement should stop
    
    private Shapes mFloor;
    private Shapes mCube;
    private Shapes mCube2;
    private Shapes mCube3;
    private Light mLight;   
    
    private RiftScreen mScreen;
    
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
    
    private List<Shapes> mShapes = new ArrayList<Shapes>();
    
    public RiftRenderer(Context context) {
    	mContext = context;
    }
    
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        
        mScreen = new RiftScreen(mContext);

        mFloor = Shapes.Floor(mContext, 1.0f).scale(SIZE_WORLD, 0.1f, SIZE_WORLD).translate(0f, -1f, 0f);
        
        mCube = Shapes.ColorCube(mContext, 1.0f).rotate(-40, 1, -1, 0).translate(0, 1.0f, 0);
        mCube2 = Shapes.ColorCube(mContext, 2.0f).translate(-3f, 1.0f, 0.0f);
        mCube3 = Shapes.ColorCube(mContext, 2.0f).translate(3f, 1.0f, 0.0f);
        
        mShapes.add(mCube);
        mShapes.add(mCube2);
        mShapes.add(mCube3);
        
        mLight = new Light(mContext);
        
        // add light to scene
        mFloor.addLight(mLight);
        mCube.addLight(mLight);
        mCube2.addLight(mLight);
        mCube3.addLight(mLight);
        
        mCamera = new RiftCamera(RiftCamera.PLAYER_IPD, RiftCamera.PLAYER_EYE_HEIGHT, RiftCamera.CAMERA_FOV, 1);
        mCamera.mPosZ = 10;
        mIPD = mCamera.getIPD();
        
        
		

    }

    @Override
    public void onDrawFrame(GL10 unused) {
    	
        movePlayer();

        updateScene();
        
        renderFrameToTexture();
       	//renderFrame();

        
    	if(frameCheckTime < System.currentTimeMillis()) {
//    		if(D) Log.d(TAG, String.format("FPS: %d, angle: %.2f, x: %.2f, y: %.2f, IPD: %.4f, FOV: %.2f",
//    						frameCounter, mCamera.mYaw, mCamera.mPosZ, mCamera.mPosX, mCamera.getIPD(), mCamera.getFOV()) );
    		frameCounter = 0;
    		frameCheckTime += 1000;
    	}
    	frameCounter++;
    }
    
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
    	
    	mScreenWidth = width;
    	mScreenHeight = height;
    	
    	mTextureWidth = width;
    	mTextureHeight = height;
    	
    	mHalfWidth = mTextureWidth / 2;
    	mHeight = mTextureHeight;
    	
        mRatio = (float) mHalfWidth / height;

        mCamera.setFOV(mCamera.getFOV(), mRatio);
        
        frameCheckTime = System.currentTimeMillis() + 1000;
        
        float ratio = (float) width / height;
        Matrix.frustumM(mPMatrix, 0, -ratio, ratio, -1, 1, 1.0f, 10);
        
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -1.0000001f, 0.0f, 0f, 0f, 0f, 1.0f, 0.0f);
        
        // Setup Render to texture
		setupRenderToTexture();
		
		mScreen.setRatio(ratio);
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
    
    
    private float angleInDegrees;
    
    private void updateScene() {
    	mCube.rotate(1.5f, 6f, 2.7f, 3.5f);
    	mCube2.rotate(-0.3f, 3f, 2.1f, 0.5f);
      
        angleInDegrees = (360.0f / 10000.0f) * ((int) (SystemClock.uptimeMillis() % 10000L)); 
    	mLight.reset().translate(-1.5f, 0, 0f).rotate(angleInDegrees, 0, 1, 0).translate(0f, 2.8f, -3f);
   	
    }
    
    private int mTextureWidth, mTextureHeight;
    private int mScreenWidth, mScreenHeight;
    int[] fb, depthRb, renderTex;
    IntBuffer texBuffer;
	private boolean aa = true;	// anti-aliasing
    
    
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
		
		renderTexture();
		
		return true;
    }
    
    private final float[] mMMatrix = new float[16];
    private final float[] mVMatrix = new float[16]; 
    private final float[] mPMatrix = new float[16]; 
    private final float[] mMVPMatrix = new float[16];
    
    
    private void renderTexture() {
    	
		GLES20.glClearColor(0f, 0f, 0f, 1f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		GLES20.glViewport(0, 0, mScreenWidth, mScreenHeight);
		
		Matrix.setIdentityM(mMMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
		Matrix.multiplyMM(mMVPMatrix, 0, mPMatrix, 0, mMVPMatrix, 0);
		
		mScreen.draw(mMVPMatrix, renderTex[0]);
		
		GLES20.glFlush();
    }
    
    private void setupRenderToTexture() {
		fb = new int[1];
		depthRb = new int[1];
		renderTex = new int[1];
		
		// generate
		GLES20.glGenFramebuffers(1, fb, 0);
		GLES20.glGenRenderbuffers(1, depthRb, 0);
		GLES20.glGenTextures(1, renderTex, 0);
		
		// generate color texture
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, renderTex[0]);

		// parameters
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

		// create it 
		// create an empty intbuffer first?
		int[] buf = new int[mTextureWidth * mTextureHeight];
		texBuffer = ByteBuffer.allocateDirect(buf.length * 4).order(ByteOrder.nativeOrder()).asIntBuffer();;
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, mTextureWidth, mTextureHeight, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_SHORT_5_6_5, texBuffer);
		
		// create render buffer and bind 16-bit depth buffer
		GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRb[0]);
		GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mTextureWidth, mTextureHeight);
    }
    
    private void renderFrame() {
    	GLES20.glClearColor(0.3f, 0.3f, 0.3f, 1f);
    	
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
        
        for(Shapes s : mShapes) {
        	s.draw(VMatrix, PMatrix);
        }

        mLight.draw(VMatrix, PMatrix);
    } 

}