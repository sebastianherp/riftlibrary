package net.appsdoneright.riftlib.util;

public class Quaternion {
	public double x,y,z,w;
	
	public Quaternion() {
		x = y = z = 0;
		w = 1;
	}
	
	public Quaternion(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	public Quaternion(Quaternion q) {
		set(q);
	}
	
	public Quaternion set(Quaternion q) {
		this.x = q.x;
		this.y = q.y;
		this.z = q.z;
		this.w = q.w;
		return this;
	}
	
	public Quaternion set(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}
	
	@Override
	public String toString() {
		return String.format("x=%.3g, y=%.3g, z=%.3g, w=%.3g", x, y, z, w);
	}
	
	public Vector3 toAngles() {
		double sqx = x*x, sqy = y*y, sqz = z*z, sqw = w*w;
		double unit = sqx + sqy + sqz + sqw;
		
		double test = x * y + z * w;
		if(test > 0.499 * unit) { // "north pole"
			return new Vector3(
				2 * (float)Math.atan2(x, w),
				(float)Math.PI/2,
				0);
		} else if(test < -0.499 * unit) { // "south pole"
			return new Vector3(
					-2 * (float)Math.atan2(x, w),
					(float)-Math.PI/2,
					0);
		} else {
			return new Vector3(
					(float)Math.atan2(2 * y * w - 2 * x * z, sqx - sqy - sqz + sqw),
					(float)Math.asin(2 * test / unit),
					(float)Math.atan2(2 * x * w - 2 * y * z, -sqx + sqy - sqz + sqw));
		}		
	}
	
	
	public float[] toMatrix() {
		float[] res = new float[16];
		toMatrix(res);
		return res;
	}
	
	public void toMatrix(float[] matrix) {
		
		this.normalize();
		
		float x2 = (float) (x * x);
		float y2 = (float) (y * y);
		float z2 = (float) (z * z);
		float xy = (float) (x * y);
		float xz = (float) (x * z);
		float yz = (float) (y * z);
		float wx = (float) (w * x);
		float wy = (float) (w * y);
		float wz = (float) (w * z);
		
		matrix[0] =	1.0f - 2.0f * (y2 + z2);
		matrix[1] = 2.0f * (xy - wz);
		matrix[2] = 2.0f * (xz + wy);
		matrix[3] = 0.0f;

		matrix[4] = 2.0f * (xy + wz);
		matrix[5] = 1.0f - 2.0f * (x2 + z2);
		matrix[6] = 2.0f * (yz - wx);
		matrix[7] = 0.0f;
		
		matrix[8] = 2.0f * (xz - wy);
		matrix[9] = 2.0f * (yz + wx);
		matrix[10] = 1.0f - 2.0f * (x2 + y2);
		matrix[11] = 0.0f;
		
		matrix[12] = 0.0f;
		matrix[13] = 0.0f;
		matrix[14] = 0.0f;
		matrix[15] = 1.0f;
	}
	
	public Quaternion multiply(Quaternion q) {
		return multiply(q.x, q.y, q.z, q.w);
	}
	
	public Quaternion multiply(double q2x, double q2y, double q2z, double q2w) {
		double q1x = this.x, q1y = this.y, q1z = this.z, q1w = this.w;
		
		this.x = q1x * q2w + q1w * q2x + q1y * q2z - q1z * q2y;
		this.y = q1y * q2w + q1w * q2y + q1z * q2x - q1x * q2z;
		this.z = q1z * q2w + q1w * q2z + q1x * q2y - q1y * q2x;
		this.w = q1w * q2w - q1x * q2x - q1y * q2y - q1z * q2z;
		
		return this;
	}
	
	public static double dot(Quaternion q1, Quaternion q2) {
		return q1.x*q2.x + q1.y*q2.y + q1.z*q2.z + q1.w*q2.w;
	}

	public double length() {
		return Math.sqrt(x*x + y*y + z*z + w*w);
	}
	
	public Quaternion normalize() {
		double len = length();
		if(len == 0) {
			x = y = z = w = 0;
		} else {
			x /= len;
			y /= len;
			z /= len;
			w /= len;
		}
		return this;
	}
	
	public Quaternion inverse() {
		double dot = dot(this, this);
		double invDot = 1.0/dot;
		
		this.x *= -invDot;
		this.y *= -invDot;
		this.z *= -invDot;
		this.w *= invDot;
		
		return this;
	}
	
}
