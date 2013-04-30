package net.appsdoneright.riftlib;

import net.appsdoneright.riftlib.util.Quaternion;
import net.appsdoneright.riftlib.util.TrackerMessage;
import net.appsdoneright.riftlib.util.Vector3;

public class RiftOrientation {
	private static final float timeUnit = (1.0f / 1000.0f);
	private static final float YAW_MULT = 1.0f;
	private static final float GAIN = 0.5f;
	private static final boolean ENABLE_GRAVITY = true;
	
	private Vector3 mAngV = new Vector3();
	private Vector3 mA = new Vector3();
	private Quaternion mOrientation = new Quaternion();
	
	
	private static class MessageBodyFrame {
		Vector3 mAcceleration = new Vector3();
		Vector3 mRotationRate = new Vector3();
		Vector3 mMagneticField = new Vector3();
		float mTemperature;
		float mTimeDelta;
	}
	
	public Quaternion getOrientation() {
		return mOrientation;
	}
	
	public void updateOrientation(TrackerMessage msg) {
		
		MessageBodyFrame sensors = new MessageBodyFrame();
		int iterations = msg.mSampleCount;
		if(msg.mSampleCount > 3) {
			iterations = 3;
			sensors.mTimeDelta = (msg.mSampleCount - 2) * timeUnit;
		} else {
			sensors.mTimeDelta = timeUnit;
		}
		
		for(int i=0; i < iterations; i++) {
			sensors.mAcceleration.set(msg.samples[i].mAcc);
			sensors.mRotationRate.set(msg.samples[i].mGyro);
			sensors.mMagneticField.set(msg.mMag);
			sensors.mTemperature = msg.mTemperature;
			
			updateOrientation(sensors);
			
			sensors.mTimeDelta = timeUnit;					
		}
		
	}
	
	private void updateOrientation(MessageBodyFrame sensors) {
		
		updateOrientationOculus(sensors);
	}
	
	/***
	 * Possible license problem, but implemented so we can compare
	 * @param sensors
	 */
	private void updateOrientationOculus(MessageBodyFrame sensors) {
		mAngV.set(sensors.mRotationRate);
		mAngV.y *= YAW_MULT;
		
		mA.set(sensors.mAcceleration).scale(sensors.mTimeDelta);
		
		Vector3 dV = new Vector3(mAngV).scale(sensors.mTimeDelta);
		final float angle = dV.length();
		
		if(angle > 0.0f) {
			float halfa = angle * 0.5f;
			float sina = (float)Math.sin(halfa) / angle;
			Quaternion dQ = new Quaternion(
				dV.x * sina,
				dV.y * sina,
				dV.z * sina,
				Math.cos(halfa)
			);
			mOrientation.multiply(dQ);
		}
		
		final float accelMagnitude = sensors.mAcceleration.length();
		final float angVMagnitude = mAngV.length();
		final float gravityEpsilon = 0.4f;
		final float angVEpsilon = 3.0f;
		
		if(ENABLE_GRAVITY && 
			(Math.abs(accelMagnitude - 9.81f) < gravityEpsilon) &&
			(angVMagnitude < angVEpsilon)) {
			
			Vector3 yUp = new Vector3(0, 1, 0);
			Vector3 aw = mOrientation.rotate(mA);
			
			Quaternion feedback = new Quaternion(
				-aw.z * GAIN,
				0,
				aw.x * GAIN,
				1);
			
			Quaternion q1 = new Quaternion(feedback).multiply(mOrientation);
			q1.normalize();
			
			float angle0 = Vector3.angle(yUp, aw);
			
			Vector3 temp = q1.rotate(mA);
			float angle1 = Vector3.angle(yUp, temp);
			
			if(angle1 < angle0) {
				mOrientation.set(q1);
			
			} else {
				
				Quaternion feedback2 = new Quaternion(
						aw.z * GAIN,
						0,
						-aw.x * GAIN,
						1);
				
				Quaternion q2 = new Quaternion(feedback2).multiply(mOrientation);
				q2.normalize();
				
				Vector3 temp2 = q2.rotate(mA);
				float angle2 = Vector3.angle(yUp, temp2);
				
				if(angle2 < angle0) {
					mOrientation.set(q2);
				}
			}
		}
	}
}
