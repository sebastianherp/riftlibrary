package net.appsdoneright.riftlib.samplegame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import net.appsdoneright.oculusrifttest.R;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class RiftScreen {

	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 8 * FLOAT_SIZE_BYTES;
	private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
	private static final int TRIANGLE_VERTICES_DATA_NOR_OFFSET = 3;
	private static final int TRIANGLE_VERTICES_DATA_TEX_OFFSET = 6;
	
	public enum Eye {
		Left, Right
	};
	
	private FloatBuffer _qvb;
	private FloatBuffer _qvb2;
	private IntBuffer _qib;
	
	private final Context mContext;
	
	private int	mProgram;
	
	private int LensCenterLocation;
    private int ScreenCenterLocation;
    private int ScaleLocation;
    private int ScaleInLocation;
    private int HmdWarpParamLocation;
    
    float mRatio = 1.0f;
	
	public RiftScreen(Context context) {
		mContext = context;
		setRatio(1.76f);
	}
	
	public void setRatio(float ratio) {
		mRatio = ratio;
		init(ratio);
	}
	
	private void init(float ratio) {
		
	    // the full-screen quad buffers
		final float x = ratio;
		final float y = 1.0f;
		final float z = 0.0f;
		// vertex information - clockwise
								// x, y, z, nx, ny, nz, u, v
		final float _quadv[] = { -x, -y, z, 0, 0, -1, 0.5f, 0,
								 -x,  y, z, 0, 0, -1, 0.5f, 1,
								  0,  y, z, 0, 0, -1, 0, 1,
								  0, -y, z, 0, 0, -1, 0, 0
							   };
		final float _quad2v[] = {  0, -y, z, 0, 0, -1, 1, 0,
								   0,  y, z, 0, 0, -1, 1, 1,
								   x,  y, z, 0, 0, -1, 0.5f, 1,
								   x, -y, z, 0, 0, -1, 0.5f, 0
			   				   };
		
		final int _quadi[] = { 0, 1, 2,
	              2, 3, 0  
				};
		
		// Setup quad 
		// Generate your vertex, normal and index buffers
		// vertex buffer
		_qvb = ByteBuffer.allocateDirect(_quadv.length
				* 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		_qvb.put(_quadv);
		_qvb.position(0);
		
		_qvb2 = ByteBuffer.allocateDirect(_quad2v.length
				* 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		_qvb2.put(_quad2v);
		_qvb2.position(0);

		// index buffer
		_qib = ByteBuffer.allocateDirect(_quadi.length
				* 4).order(ByteOrder.nativeOrder()).asIntBuffer();
		_qib.put(_quadi);
		_qib.position(0);
		
		int vertexShader = Shapes.loadShader(mContext, GLES20.GL_VERTEX_SHADER, R.raw.rift_vs);
		int fragmentShader = Shapes.loadShader(mContext, GLES20.GL_FRAGMENT_SHADER, R.raw.rift_ps);
		
		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertexShader);
		Shapes.checkGlError("glAttachShader");
		GLES20.glAttachShader(mProgram, fragmentShader);
		Shapes.checkGlError("glAttachShader");
		GLES20.glLinkProgram(mProgram);
		Shapes.checkGlError("glLinkProgram");
		
		LensCenterLocation = GLES20.glGetUniformLocation(mProgram, "LensCenter");
        ScreenCenterLocation = GLES20.glGetUniformLocation(mProgram, "ScreenCenter");
        ScaleLocation = GLES20.glGetUniformLocation(mProgram, "Scale");
        ScaleInLocation = GLES20.glGetUniformLocation(mProgram, "ScaleIn");
        HmdWarpParamLocation = GLES20.glGetUniformLocation(mProgram, "HmdWarpParam");
	
	}
	
	public static float K0 = 1.0f;
    public static float K1 = 0.22f;
    public static float K2 = 0.24f;
    public static float K3 = 0.0f;
	
	public void renderDistortedEye(float eye, float x, float y, float w, float h) {
        float as = w/h;
        
        float scaleFactor = mRatio/2;
        
        float DistortionXCenterOffset;
        DistortionXCenterOffset = eye;
        
        GLES20.glUniform2f(LensCenterLocation, x + (w + DistortionXCenterOffset * 0.5f)*0.5f, y + h*0.5f);
        Shapes.checkGlError("glUniform2f");
        GLES20.glUniform2f(ScreenCenterLocation, x + w*0.5f, y + h*0.5f);
        Shapes.checkGlError("glUniform2f");
        GLES20.glUniform2f(ScaleLocation, (w/2.0f) * scaleFactor, (h/2.0f) * scaleFactor * as);;
        Shapes.checkGlError("glUniform2f");
        GLES20.glUniform2f(ScaleInLocation, (2.0f/w), (2.0f/h) / as);
        Shapes.checkGlError("glUniform2f");

        GLES20.glUniform4f(HmdWarpParamLocation, K0, K1, K2, K3);
        Shapes.checkGlError("glUniform4f");
	}
	
	public void draw(float[] mMVPMatrix, int texId) {
		draw(Eye.Left, mMVPMatrix, texId);
		draw(Eye.Right, mMVPMatrix, texId);
	}
	
	public void draw(Eye eye, float[] mMVPMatrix, int texId) {
		GLES20.glUseProgram(mProgram);

		GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(mProgram, "uMVPMatrix"), 1, false, mMVPMatrix, 0);
		
		// bind the framebuffer texture
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
		GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgram, "texture1"), 0);
		

		// the vertex coordinates
		if(eye == Eye.Left) {
			// texture coordinates
			_qvb.position(TRIANGLE_VERTICES_DATA_TEX_OFFSET);
			GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(mProgram, "textureCoord"), 2, GLES20.GL_FLOAT, false,
					TRIANGLE_VERTICES_DATA_STRIDE_BYTES, _qvb);
			GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(mProgram, "textureCoord"));

			_qvb.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
			GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(mProgram, "aPosition"), 3, GLES20.GL_FLOAT, false,
					TRIANGLE_VERTICES_DATA_STRIDE_BYTES, _qvb);
			GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(mProgram, "aPosition"));
	
			renderDistortedEye(-0.25f, 0.0f, 0.0f, 0.5f, 1.0f);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, _qib);
			GLES20.glFlush();
		} else {
			// texture coordinates
			_qvb2.position(TRIANGLE_VERTICES_DATA_TEX_OFFSET);
			GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(mProgram, "textureCoord"), 2, GLES20.GL_FLOAT, false,
					TRIANGLE_VERTICES_DATA_STRIDE_BYTES, _qvb2);
			GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(mProgram, "textureCoord"));

			// the vertex coordinates
			_qvb2.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
			GLES20.glVertexAttribPointer(GLES20.glGetAttribLocation(mProgram, "aPosition"), 3, GLES20.GL_FLOAT, false,
					TRIANGLE_VERTICES_DATA_STRIDE_BYTES, _qvb2);
			GLES20.glEnableVertexAttribArray(GLES20.glGetAttribLocation(mProgram, "aPosition"));
			
			// Draw with indices
			renderDistortedEye(0.25f, 0.5f, 0.0f, 0.5f, 1.0f);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, _qib);
			GLES20.glFlush();
		}
		

	}
	
}
