package net.appsdoneright.riftlib.samplegame;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import net.appsdoneright.oculusrifttest.R;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class Shapes {
	private static final String TAG = Shapes.class.getSimpleName();
	
	private Context mContext = null;
	
    private FloatBuffer mVertices;
    private FloatBuffer mNormals;
    private FloatBuffer mTexCoords;
    private ShortBuffer mIndices;
    private int mNumIndices;
    
    private boolean mIsLight = false;
    
    private int	mProgram,
		maPositionHandle,
		maNormalHandle,
    	muLightPosHandle,
		muPMatrixHandle,
		muMMatrixHandle,
		muVMatrixHandle,
		msSamplerHandle,
		muMatAmbientHandle,
		muMatDiffuseHandle,
		muMatSpecularHandle,
		muMatShininessHandle;
    
	private int mTextureId;

	private float[] mMMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];
	
	private List<Light> mLights = new ArrayList<Light>();
	
	// material properties
	private float[] matAmbient;
	private float[] matDiffuse;
	private float[] matSpecular;
	private float matShininess;
	
	public Shapes(Context context) {
		mContext = context;
	}

	public static Shapes Floor(Context context, float scale) {
		Shapes shape = new Shapes(context);
		shape.genCube(scale);
		shape.createSkyTextureCubemap();
		shape.init();
		
		return shape;
	}
	
	public static Shapes ColorCube(Context context, float scale) {
		Shapes shape = new Shapes(context);
		shape.genCube(scale);
		shape.createSimpleTextureCubemap();
		shape.init();
		
		return shape;
	}
	
	
	public void addLight(Light light) {
		mLights.add(light);
	}
	
	public void clearLights() {
		mLights.clear();
	}
	
	private void genCube(float scale) {
        int i;
        int numVertices = 24;
        int numIndices = 36;
                   
        float[] cubeVerts =
           {
              -0.5f, -0.5f, -0.5f,
              -0.5f, -0.5f,  0.5f,
              0.5f, -0.5f,  0.5f,
              0.5f, -0.5f, -0.5f,
              -0.5f,  0.5f, -0.5f,
              -0.5f,  0.5f,  0.5f,
              0.5f,  0.5f,  0.5f,
              0.5f,  0.5f, -0.5f,
              -0.5f, -0.5f, -0.5f,
              -0.5f,  0.5f, -0.5f,
              0.5f,  0.5f, -0.5f,
              0.5f, -0.5f, -0.5f,
              -0.5f, -0.5f, 0.5f,
              -0.5f,  0.5f, 0.5f,
              0.5f,  0.5f, 0.5f, 
              0.5f, -0.5f, 0.5f,
              -0.5f, -0.5f, -0.5f,
              -0.5f, -0.5f,  0.5f,
              -0.5f,  0.5f,  0.5f,
              -0.5f,  0.5f, -0.5f,
              0.5f, -0.5f, -0.5f,
              0.5f, -0.5f,  0.5f,
              0.5f,  0.5f,  0.5f,
              0.5f,  0.5f, -0.5f,
           };

        float[] cubeNormals =
           {
              0.0f, -1.0f, 0.0f,
              0.0f, -1.0f, 0.0f,
              0.0f, -1.0f, 0.0f,
              0.0f, -1.0f, 0.0f,
              0.0f, 1.0f, 0.0f,
              0.0f, 1.0f, 0.0f,
              0.0f, 1.0f, 0.0f,
              0.0f, 1.0f, 0.0f,
              0.0f, 0.0f, -1.0f,
              0.0f, 0.0f, -1.0f,
              0.0f, 0.0f, -1.0f,
              0.0f, 0.0f, -1.0f,
              0.0f, 0.0f, 1.0f,
              0.0f, 0.0f, 1.0f,
              0.0f, 0.0f, 1.0f,
              0.0f, 0.0f, 1.0f,
              -1.0f, 0.0f, 0.0f,
              -1.0f, 0.0f, 0.0f,
              -1.0f, 0.0f, 0.0f,
              -1.0f, 0.0f, 0.0f,
              1.0f, 0.0f, 0.0f,
              1.0f, 0.0f, 0.0f,
              1.0f, 0.0f, 0.0f,
              1.0f, 0.0f, 0.0f,
           };

        float[] cubeTex =
           {
              0.0f, 0.0f,
              0.0f, 1.0f,
              1.0f, 1.0f,
              1.0f, 0.0f,
              1.0f, 0.0f,
              1.0f, 1.0f,
              0.0f, 1.0f,
              0.0f, 0.0f,
              0.0f, 0.0f,
              0.0f, 1.0f,
              1.0f, 1.0f,
              1.0f, 0.0f,
              0.0f, 0.0f,
              0.0f, 1.0f,
              1.0f, 1.0f,
              1.0f, 0.0f,
              0.0f, 0.0f,
              0.0f, 1.0f,
              1.0f, 1.0f,
              1.0f, 0.0f,
              0.0f, 0.0f,
              0.0f, 1.0f,
              1.0f, 1.0f,
              1.0f, 0.0f,
           };
                   
                  
        // Allocate memory for buffers
        mVertices = ByteBuffer.allocateDirect(numVertices * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mNormals = ByteBuffer.allocateDirect(numVertices * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTexCoords = ByteBuffer.allocateDirect(numVertices * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mIndices = ByteBuffer.allocateDirect(numIndices * 2).order(ByteOrder.nativeOrder()).asShortBuffer();

        
        for ( i = 0; i < cubeVerts.length; i++ )
        	cubeVerts[i] *= scale;

        mVertices.put(cubeVerts).position(0);
        mNormals.put(cubeNormals).position(0);
        mTexCoords.put(cubeTex).position(0);
        
        short[] cubeIndices =
          {
             0, 2, 1,
             0, 3, 2, 
             4, 5, 6,
             4, 6, 7,
             8, 9, 10,
             8, 10, 11, 
             12, 15, 14,
             12, 14, 13, 
             16, 17, 18,
             16, 18, 19, 
             20, 23, 22,
             20, 22, 21
          };

        mIndices.put(cubeIndices).position(0);
        mNumIndices = numIndices;
        
        float[] mA = {0.7f, 0.7f, 0.7f, 1.0f};
		matAmbient = mA;

		float[] mD = {0.5f, 0.5f, 0.5f, 1.0f};
		matDiffuse = mD;

		float[] mS =  {0.5f, 0.5f, 0.5f, 0.5f};
		matSpecular = mS;

		matShininess = 5.0f;
    }
	
	private int createSimpleTextureCubemap( )
    {
        int[] textureId = new int[1];

        // Face 0 - Red        
        byte[] cubePixels0 = { 127, 0, 0 };
        // Face 1 - Green
        byte[] cubePixels1 = { 0, 127, 0 }; 
        // Face 2 - Blue
        byte[] cubePixels2 = { 0, 0, 127 };
        // Face 3 - White
        byte[] cubePixels3 = { 127, 127, 127 };
        // Face 4 - Purple
        byte[] cubePixels4 = { 127, 0, 127 };
        // Face 5 - Yellow
        byte[] cubePixels5 = { 127, 127, 0 };
                
        ByteBuffer cubePixels = ByteBuffer.allocateDirect(3);
    
        // Generate a texture object
        GLES20.glGenTextures ( 1, textureId, 0 );

        // Bind the texture object
        GLES20.glBindTexture ( GLES20.GL_TEXTURE_CUBE_MAP, textureId[0] );
    
        // Load the cube face - Positive X
        cubePixels.put(cubePixels0).position(0);
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Negative X
        cubePixels.put(cubePixels1).position(0);
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Positive Y
        cubePixels.put(cubePixels2).position(0);        
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Negative Y
        cubePixels.put(cubePixels3).position(0);
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Positive Z
        cubePixels.put(cubePixels4).position(0);        
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Negative Z
        cubePixels.put(cubePixels5).position(0);
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Set the filtering mode
        GLES20.glTexParameteri ( GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST );
        GLES20.glTexParameteri ( GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST );

        mTextureId = textureId[0];        
        return textureId[0];
    }
	
	private int createSkyTextureCubemap( )
    {
        int[] textureId = new int[1];

        // Face 0 - Blue        
        byte[] cubePixelsSky = { 63, 63, 127 };
        // Face 3 - Gray
        byte[] cubePixelsFloor = { 63, 127, 63 };
                
        ByteBuffer cubePixels = ByteBuffer.allocateDirect(3);
    
        // Generate a texture object
        GLES20.glGenTextures ( 1, textureId, 0 );

        // Bind the texture object
        GLES20.glBindTexture ( GLES20.GL_TEXTURE_CUBE_MAP, textureId[0] );
    
        // Load the cube face - Positive X
        cubePixels.put(cubePixelsFloor).position(0);
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Negative X
        cubePixels.put(cubePixelsFloor).position(0);
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Positive Y
        cubePixels.put(cubePixelsFloor).position(0);        
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Negative Y
        cubePixels.put(cubePixelsFloor).position(0);
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Positive Z
        cubePixels.put(cubePixelsFloor).position(0);        
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Negative Z
        cubePixels.put(cubePixelsFloor).position(0);
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Set the filtering mode
        GLES20.glTexParameteri ( GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST );
        GLES20.glTexParameteri ( GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST );

        mTextureId = textureId[0];
        return textureId[0];
    }	

	private void init() {
		
		int vertexShader = loadShader(mContext, GLES20.GL_VERTEX_SHADER, R.raw.vs_basic);
		int fragmentShader = loadShader(mContext, GLES20.GL_FRAGMENT_SHADER, R.raw.ps_basic);
		
		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertexShader);
		GLES20.glAttachShader(mProgram, fragmentShader);
		GLES20.glLinkProgram(mProgram);
		
		//get handle to the vertex shaders vPos member
		maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
		maNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
		muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
		muVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uVMatrix");
		muPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uPMatrix");
		muLightPosHandle = GLES20.glGetUniformLocation(mProgram, "uLightPos");
		msSamplerHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");
		muMatAmbientHandle = GLES20.glGetUniformLocation(mProgram, "matAmbient");
		muMatDiffuseHandle = GLES20.glGetUniformLocation(mProgram, "matDiffuse");
		muMatSpecularHandle = GLES20.glGetUniformLocation(mProgram, "matSpecular");
		muMatShininessHandle = GLES20.glGetUniformLocation(mProgram, "matShininess");

		
		Matrix.setIdentityM(mMMatrix, 0);
	}
    
    public FloatBuffer getVertices()
    {
    	return mVertices;
    }
    
    public FloatBuffer getNormals()
    {
    	return mNormals;
    }
    
    public FloatBuffer getTexCoords()
    {
    	return mTexCoords;
    }
    
    public ShortBuffer getIndices()
    {
    	return mIndices;
    }
    
    public int getNumIndices()
    {
    	return mNumIndices;
    }
    
    public float[] getMMatrix() {
    	return mMMatrix;
    }
                    
    public void draw(float[] mVMatrix, float[] mProjMatrix) {
		//Add program
		GLES20.glUseProgram(mProgram);
		
		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertices);
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT, false, 0, mNormals);
		GLES20.glEnableVertexAttribArray(maNormalHandle);
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, mTextureId);
		
		GLES20.glUniform1i(msSamplerHandle, 0);
		
        GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, mMMatrix, 0);
        GLES20.glUniformMatrix4fv(muVMatrixHandle, 1, false, mVMatrix, 0);
		GLES20.glUniformMatrix4fv(muPMatrixHandle, 1, false, mProjMatrix, 0);
		
		if(mLights.size() > 0) {
			float[] mLightPosInEyeSpace = mLights.get(0).getLightPos();
			GLES20.glUniform3f(muLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);
			
			GLES20.glUniform4fv(muMatAmbientHandle, 1, matAmbient, 0);
			GLES20.glUniform4fv(muMatDiffuseHandle, 1, matDiffuse, 0);
			GLES20.glUniform4fv(muMatSpecularHandle, 1, matSpecular, 0);
			GLES20.glUniform1f(muMatShininessHandle, matShininess);
		}

		//Draw the shape
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, mNumIndices, GLES20.GL_UNSIGNED_SHORT, mIndices);
    }

    public Shapes translate(float x, float y, float z) {
    	Matrix.translateM(mMMatrix, 0, x, y, z);
    	return this;
    }
    
    public Shapes rotate(float angle, float x, float y, float z) {
    	Matrix.rotateM(mMMatrix, 0, angle, x, y, z);
    	return this;
    }

    public Shapes scale(float x, float y, float z) {
    	Matrix.scaleM(mMMatrix, 0, x, y, z);
    	return this;
    }
    
    public static int loadShader(Context context, int type, int resourceID) {
    	StringBuffer shader = new StringBuffer();
    	
    	try {
	    	InputStream is = context.getResources().openRawResource(resourceID);
	    	BufferedReader br = new BufferedReader(new InputStreamReader(is));
	    	String read = br.readLine();
			while (read != null) {
				shader.append(read + "\n");
				read = br.readLine();
			}
	
			shader.deleteCharAt(shader.length() - 1);
    	} catch(Exception e) {
    		Log.d(TAG, "Could not read shader: " + e.getMessage());
    	}
    	
    	return loadShader(type, shader.toString());
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
