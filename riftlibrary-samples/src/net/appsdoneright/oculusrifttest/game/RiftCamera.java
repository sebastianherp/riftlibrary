package net.appsdoneright.oculusrifttest.game;

import net.appsdoneright.riftlib.util.Quaternion;
import android.opengl.Matrix;

public class RiftCamera {
	public float mPosX, mPosY, mPosZ;
	public float mYaw, mPitch, mRoll;
	
	private float mIPD, mEyeHeight, mFOV;
	
	private final float mHMatrix[] = new float[16];
	private final float mVMatrix[] = new float[16];

	public final float mVMatrixLeft[] = new float[16];
	public final float mVMatrixRight[] = new float[16];
	public final float[] mProjMatrix = new float[16]; // need one for left and right? one should be ok for now
	
	public RiftCamera() {
		this(0.227f, 1.83f);
	}
	
	public RiftCamera(float IPD, float eyeHeight) {
		setIPD(IPD);
		mEyeHeight = eyeHeight;
	}


	
	public void update() {
		// replace with rotation matrix from quaternion
		//Matrix.setRotateEulerM(mVMatrix, 0, mPitch, mYaw, mRoll);
		Matrix.setRotateM(mVMatrix, 0, mYaw, 0, 1, 0);
		Matrix.rotateM(mVMatrix, 0, mPitch, 1, 0, 0);
		Matrix.rotateM(mVMatrix, 0, mRoll, 0, 0, 1);

		Matrix.multiplyMM(mVMatrix, 0, mHMatrix, 0, mVMatrix, 0);
		
		Matrix.translateM(mVMatrix, 0, -mPosX, -mPosY, -mPosZ);
		
		float cosAngle = (float)Math.cos(mYaw / 180.0 * Math.PI);
    	float singAngle = (float)Math.sin(mYaw / 180.0 * Math.PI);
		
		// left eye
		Matrix.translateM(mVMatrixLeft, 0, mVMatrix, 0, -cosAngle * mIPD, -mEyeHeight, -singAngle * mIPD);
		// right eye
		Matrix.translateM(mVMatrixRight, 0, mVMatrix, 0, cosAngle * mIPD, -mEyeHeight, singAngle * mIPD);

	}
	
	public void setHeadOrientation(Quaternion q) {
		q.toMatrix(mHMatrix);
	}
	
	public float getIPD() {
		return -mIPD * 2.0f;
	}
	
	public void setIPD(float IPD) {
		mIPD = -IPD/2.0f;
	}
	
	public float getEyeHeight() {
		return mEyeHeight;
	}
	
	public void setEyeHeight(float eyeHeight) {
		mEyeHeight = eyeHeight;
	}
	
	public void setFOV(float FOV, float ratio) {
		mFOV = FOV;
		perspectiveM(mProjMatrix, 0, mFOV, ratio, 0.1f, 150);
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
	
}
