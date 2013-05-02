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

class Triangle {

    private final String vertexShaderCode =
        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +

        "attribute vec4 vPosition;" +
        "void main() {" +
        // the matrix must be included as a modifier of gl_Position
        "  gl_Position = vPosition * uMVPMatrix;" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = vColor;" +
        "}";

    private final FloatBuffer vertexBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = { // in counterclockwise order:
         0.0f,  0.622008459f, 0.0f,   // top
        -0.5f, -0.311004243f, 0.0f,   // bottom left
         0.5f, -0.311004243f, 0.0f    // bottom right
    };
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    public Triangle() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = RiftRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                                                   vertexShaderCode);
        int fragmentShader = RiftRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                     fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables

    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                     GLES20.GL_FLOAT, false,
                                     vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        RiftRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        RiftRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}

class Square {

    private final String vertexShaderCode =
        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +

        "attribute vec4 vPosition;" +
        "void main() {" +
        // the matrix must be included as a modifier of gl_Position
        "  gl_Position = vPosition * uMVPMatrix;" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = vColor;" +
        "}";

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = { -0.5f,  0.5f, 0.0f,   // top left
                                    -0.5f, -0.5f, 0.0f,   // bottom left
                                     0.5f, -0.5f, 0.0f,   // bottom right
                                     0.5f,  0.5f, 0.0f }; // top right

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    public Square() {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = RiftRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                                                   vertexShaderCode);
        int fragmentShader = RiftRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                     fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                     GLES20.GL_FLOAT, false,
                                     vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        RiftRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        RiftRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                              GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
