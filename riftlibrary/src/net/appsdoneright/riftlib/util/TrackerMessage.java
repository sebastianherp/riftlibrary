package net.appsdoneright.riftlib.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TrackerMessage {
	private static final float SENSOR_SCALE = 0.0001f;
	private static final float TEMPERATURE_SCALE = 0.01f;
	
	public byte mSampleCount;
	public int mTimestamp;
	public int mLastCommandId;
	public float mTemperature;
	
	public static class TrackerData {
		public Vector3 mAcc = new Vector3();
		public Vector3 mGyro = new Vector3();
	}
	public TrackerData[] samples = new TrackerData[3];
	public Vector3 mMag = new Vector3();
	
	public TrackerMessage() {
		for(int i=0; i<3; i++)
			samples[i] = new TrackerData();
		
	}
	
	public boolean parseBuffer(byte[] buffer, int length) {
		if(length == 62) {
			
			//bb.order(ByteOrder.LITTLE_ENDIAN);
			this.mSampleCount = buffer[1];
			this.mTimestamp = decodeUInt16(buffer, 2);
			this.mLastCommandId = decodeUInt16(buffer, 4);
			this.mTemperature = decodeSInt16(buffer, 6) * TEMPERATURE_SCALE;
			
			int iterationCount = Math.min(3, this.mSampleCount);
			for(int i=0; i < iterationCount; i++) {
				//this.samples[i] = new TrackerData();
				unpackSensor(this.samples[i].mAcc, buffer, 8 + 16 * i).scale(SENSOR_SCALE);
				unpackSensor(this.samples[i].mGyro, buffer, 16 + 16 * i).scale(SENSOR_SCALE);
			}
			
			this.mMag.set(
					decodeSInt16(buffer, 56),
					decodeSInt16(buffer, 58),
					decodeSInt16(buffer, 60)
			).scale(SENSOR_SCALE);
			
			return true;
			
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "TS: " + mTimestamp + ", Temp: " + mTemperature + " °C" +
			"\nAcc:\n"+ samples[0].mAcc.x + " m/s^2\n" + samples[0].mAcc.y + " m/s^2\n" + samples[0].mAcc.z + " m/s^2" + 
			"\nGyro:\n"+ samples[0].mGyro.x + " rad/s\n" + samples[0].mGyro.y + " rad/s\n" + samples[0].mGyro.z + " rad/s" +
			"\nMag:\n"+ mMag.x + "\n" + mMag.y + "\n" + mMag.z;
	}
	
	static int decodeUInt16(byte[] buffer, int start)
	{
		return (buffer[start+1] << 8 | buffer[start]) & 0xffff;
		//return (bb.getShort(start) & 0xffff);
	}

	static short decodeSInt16(byte[] buffer, int start)
	{
		return (short)(buffer[start+1] << 8 | buffer[start]);
	    //return bb.getShort(start);
	}

	static long decodeUInt32(ByteBuffer bb, int start)
	{    
		return (bb.getInt() & 0xffffffff);
	}

	static float decodeFloat(ByteBuffer bb, int start)
	{
		return bb.getFloat(start);
	}

	static Vector3 unpackSensor(Vector3 result, byte[] buffer, int start)
	{
		result.set(
				( buffer[start+0] << 24 | (buffer[start+1] & 0xff) << 16 | (buffer[start+2] & 0xff) << 8 ) >> 11,
				( buffer[start+2] << 29 | (buffer[start+3] & 0xff) << 21 | (buffer[start+4] & 0xff) << 13 | (buffer[start+5] & 0xff) << 5 ) >> 11,
				( buffer[start+5] << 26 | (buffer[start+6] & 0xff) << 18 | (buffer[start+7] & 0xff) << 10 ) >> 11
		);
		
		return result;
	}
}
