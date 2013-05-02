package net.appsdoneright.oculusrifttest.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.appsdoneright.riftlib.util.Quaternion;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

public class RiftRenderer implements Renderer {
    private static final String TAG = "MyGLRenderer";
    private static final boolean D = true;
    private static final boolean LEFTEYE = true;
    private static final boolean RIGHTEYE = false;
    
    private Shapes mRoom = new Shapes();
    private Shapes mCube = new Shapes();
    private Shapes mCube2 = new Shapes();
    
    private RiftCamera mCamera;
    
    private int mHalfWidth, mHeight;

    private final float[] mProjMatrixLeft = new float[16];
    private final float[] mProjMatrixRight = new float[16];

    // Declare as volatile because we are updating it from another thread
    public volatile float mdAngle;
    public volatile float mdPosX = 0;
    public volatile float mdPosY = 0;
    
    public volatile float mIPD = 1.0f;
    
    public volatile Quaternion mQuaternion = new Quaternion();
    
    private float roomSize = 40f; // in meter
    private float playerSize = 1f; // distance to wall where movement should stop
    
    private int frameCounter = 0;
    private long frameCheckTime = 0;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        mRoom.genSkyBox(1.0f).scale(roomSize, 3f, roomSize).translate(0f, 0.5f, 0f);
        
        mCube.genColorCube(1.0f).rotate(-40, 1, -1, 0).translate(0, 1.0f, 0);
        mCube2.genColorCube(2.0f).translate(-3f, 1.0f, 0.0f);
        
        mCamera = new RiftCamera();
        mCamera.mPosZ = 10;
        mIPD = mCamera.getIPD();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
    	// clear background
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        movePlayer();
        updateScene();
        
        
        // view for left eye
       	GLES20.glViewport(0, 0, mHalfWidth, mHeight);
        // draw scene
       	drawScene(mCamera.mVMatrixLeft, mProjMatrixLeft);
        // flush
        GLES20.glFlush();
        
        
        // view for right eye
       	GLES20.glViewport(mHalfWidth, 0, mHalfWidth, mHeight);
    	// draw scene
       	drawScene(mCamera.mVMatrixRight, mProjMatrixRight);
        // flush
        GLES20.glFlush();
        
    	if(frameCheckTime < System.currentTimeMillis()) {
    		if(D) Log.d(TAG, "FPS: " + frameCounter + ", angle: " + mCamera.mYaw + ", x: " + mCamera.mPosZ + ", y: " + mCamera.mPosX + ", IPD: " + mCamera.getIPD());
    		frameCounter = 0;
    		frameCheckTime += 1000;
    	}
    	frameCounter++;
    }
    
    private void movePlayer() {
    	mCamera.mYaw += mdAngle;
    	float cosAngle = (float)Math.cos(mCamera.mYaw / 180.0 * Math.PI);
    	float singAngle = (float)Math.sin(mCamera.mYaw / 180.0 * Math.PI);
    	
        mCamera.mPosZ += cosAngle * mdPosX + singAngle * mdPosY;
        mCamera.mPosX += cosAngle * mdPosY - singAngle * mdPosX;
        mCamera.setIPD(mIPD);

        mdAngle = 0;
        mdPosX = 0;
        mdPosY = 0;
        
    	// collision with room walls?
    	float border = roomSize/2 - playerSize;
    	mCamera.mPosZ = Math.min(border, Math.max(-border, mCamera.mPosZ));
    	mCamera.mPosX = Math.min(border, Math.max(-border, mCamera.mPosX));


        
        mCamera.update(); 
    }
    
    private void updateScene() {
    	mCube.rotate(1.5f, 6f, 2.7f, 3.5f);
    }
    
    private void drawScene(float[] VMatrix, float[] PMatrix) {
    	GLES20.glDisable(GLES20.GL_DEPTH_TEST);
    	mRoom.draw(VMatrix, PMatrix);
    	
    	GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    	//GLES20.glCullFace(GLES20.GL_BACK);
        //GLES20.glEnable(GLES20.GL_CULL_FACE);
        
        mCube.draw(VMatrix, PMatrix);
        mCube2.draw(VMatrix, PMatrix);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
    	
    	mHalfWidth = width/2;
    	mHeight = height;
    	
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        //GLES20.glViewport(0, 0, width, height);

        float ratio = (float) mHalfWidth / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM(mProjMatrixLeft, 0, -ratio, ratio, -1, 1, 1, 10);
        //Matrix.frustumM(mProjMatrixRight, 0, -ratio, ratio, -1, 1, 1, 10);
        
        perspectiveM(mProjMatrixLeft, 0, 45.0f, ratio, 0.1f, 150);
        perspectiveM(mProjMatrixRight, 0, 45.0f, ratio, 0.1f, 150);
        
        frameCheckTime = System.currentTimeMillis() + 1000;;

    }
    
   
    public static void perspectiveM(float[] projMatrix, int offset, float fovY, float aspect, float zNear, float zFar)
    {
        fovY = (float) ((fovY /180.0) * Math.PI); // degrees to radians
        float g = (float) (1 / Math.tan(fovY / 2));

        for(int i=0; i<16; i++) {
            switch (i) {

            case 0:
                projMatrix[i] = g / aspect;
                break;
            case 5:
                projMatrix[i] = g;
                break;
            case 10:
                projMatrix[i] = (zFar + zNear)/(zNear - zFar);
                break;
            case 11:
                projMatrix[i] = -1.0f;
                break;
            case 14:
                projMatrix[i] = (2 * zFar * zNear)/(zNear - zFar);
                break;
            default:
                projMatrix[i] = 0.0f;
            }
        }
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}