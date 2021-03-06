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
	private MessageBodyFrame mSensors = new MessageBodyFrame();
	
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
		
		int iterations = msg.mSampleCount;
		if(msg.mSampleCount > 3) {
			iterations = 3;
			mSensors.mTimeDelta = (msg.mSampleCount - 2) * timeUnit;
		} else {
			mSensors.mTimeDelta = timeUnit;
		}
		
		for(int i=0; i < iterations; i++) {
			mSensors.mAcceleration.set(msg.samples[i].mAcc);
			mSensors.mRotationRate.set(msg.samples[i].mGyro);
			mSensors.mMagneticField.set(msg.mMag);
			mSensors.mTemperature = msg.mTemperature;
			
			updateOrientation(mSensors);
			
			mSensors.mTimeDelta = timeUnit;					
		}
		
	}
	
	private void updateOrientation(MessageBodyFrame sensors) {
		
		updateOrientationOculus(sensors);
	}
	
	private Quaternion dQ = new Quaternion();
	private Quaternion feedback = new Quaternion();
	private Quaternion feedback2 = new Quaternion();
	private Quaternion q1 = new Quaternion();
	private Quaternion q2 = new Quaternion();	
	private Vector3 dV = new Vector3();
	private Vector3 aw = new Vector3();
	private Vector3 tempV = new Vector3();
	private Vector3 yUp = new Vector3();
	
	/***
	 * Possible license problem, but implemented so we can compare
	 * @param sensors
	 */
	private void updateOrientationOculus(MessageBodyFrame sensors) {
		mAngV.set(sensors.mRotationRate);
		mAngV.y *= YAW_MULT;
		
		mA.set(sensors.mAcceleration).scale(sensors.mTimeDelta);
		
		dV.set(mAngV).scale(sensors.mTimeDelta);
		final float angle = dV.length();
		
		if(angle > 0.0f) {
			float halfa = angle * 0.5f;
			float sina = (float)Math.sin(halfa) / angle;
			dQ.set(
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
			
			yUp.set(0, 1, 0);
			rotate(aw, mOrientation, mA);
			
			feedback.set(
				-aw.z * GAIN,
				0,
				aw.x * GAIN,
				1);
			
			q1.set(feedback).multiply(mOrientation);
			q1.normalize();
			
			float angle0 = Vector3.angle(yUp, aw);
			
			rotate(tempV, q2, mA);
			float angle1 = Vector3.angle(yUp, tempV);
			
			if(angle1 < angle0) {
				mOrientation.set(q1);
			
			} else {
				
				feedback2.set(
						aw.z * GAIN,
						0,
						-aw.x * GAIN,
						1);
				
				q2.set(feedback2).multiply(mOrientation);
				q2.normalize();
				
				rotate(tempV, q2, mA);
				float angle2 = Vector3.angle(yUp, tempV);
				
				if(angle2 < angle0) {
					mOrientation.set(q2);
				}
			}
		}
	}
	
	private Quaternion tempQ = new Quaternion();
	private Quaternion invQ = new Quaternion();
	
	private Vector3 rotate(Vector3 result, Quaternion q, Vector3 v) {
		tempQ.set(q);
		invQ.set(q).inverse();
		
		tempQ.multiply(v.x, v.y, v.z, 1);
		tempQ.multiply(invQ);
		
		result.set((float)tempQ.x, (float)tempQ.y, (float)tempQ.z);
		return result;
	}
}
