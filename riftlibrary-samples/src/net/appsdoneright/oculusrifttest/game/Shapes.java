package net.appsdoneright.oculusrifttest.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class Shapes {

    private FloatBuffer mVertices;
    private FloatBuffer mNormals;
    private FloatBuffer mTexCoords;
    private ShortBuffer mIndices;
    private int mNumIndices;
    
    private int	mProgram,
		maPositionHandle,
		maNormalHandle,
		muPMatrixHandle,
		muMMatrixHandle,
		muVMatrixHandle,
		msSamplerHandle;
    
	private int mTextureId;

	private float[] mMMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];
    
	private final String vertexShaderCode =
		"uniform mat4 uMMatrix;	\n" +
		"uniform mat4 uVMatrix;	\n" +
		"uniform mat4 uPMatrix;	\n" +
		"attribute vec3 aNormal;	\n" +
		"attribute vec4 aPosition;	\n" +
		"varying vec3 vNormal;		\n" +
		"void main(){				\n" +
		"	vNormal = aNormal;		\n" +
		"	gl_Position = uPMatrix * uVMatrix * uMMatrix * aPosition;\n" + // * uVMatrix * uPMatrix 
		"}							\n";

	private final String fragmentShaderCode =
		"precision mediump float;	\n" +
		"varying vec3 vNormal;		\n" +
		"uniform samplerCube sTexture;		\n" +
		"void main(){				\n" +
		"	gl_FragColor = textureCube(sTexture, vNormal);	\n" +
		//"	gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0); \n" +
		"}							\n";

	public Shapes genSkyBox(float scale) {
		mTextureId = createSkyTextureCubemap();
		return genCube(scale);
	}
	
	public Shapes genColorCube(float scale) {
		mTextureId = createSimpleTextureCubemap();
		return genCube(scale);
	}
    
	
	private Shapes genCube(float scale) {
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
        {
        	cubeVerts[i] *= scale;
        }
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
        
        init();
        return this;              
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
        cubePixels.put(cubePixelsSky).position(0);
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Negative X
        cubePixels.put(cubePixelsSky).position(0);
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Positive Y
        cubePixels.put(cubePixelsSky).position(0);        
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Negative Y
        cubePixels.put(cubePixelsFloor).position(0);
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Positive Z
        cubePixels.put(cubePixelsSky).position(0);        
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Load the cube face - Negative Z
        cubePixels.put(cubePixelsSky).position(0);
        GLES20.glTexImage2D ( GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GLES20.GL_RGB, 1, 1, 0, 
                              GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, cubePixels );

        // Set the filtering mode
        GLES20.glTexParameteri ( GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST );
        GLES20.glTexParameteri ( GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST );

        return textureId[0];
    }	
	
	private void init() {
		int vertexShader = RiftRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		int fragmentShader = RiftRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		
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
		
		msSamplerHandle = GLES20.glGetUniformLocation(mProgram, "sTexture");
		
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
		
		//Prepare the cube data
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

		//Draw the cube
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
}
