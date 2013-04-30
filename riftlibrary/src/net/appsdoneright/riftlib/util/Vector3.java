package net.appsdoneright.riftlib.util;

public class Vector3 {
	public float x,y,z;
	
	public Vector3() {
		x = y = z = 0;
	}
	
	public Vector3(float x, float y, float z) {
		set(x, y, z);
	}
	
	public Vector3(Vector3 v) {
		set(v);
	}

	public Vector3 set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	@Override
	public String toString() {
		return String.format("x=%.3g, y=%.3g, z=%.3g", x, y, z);
	}
	
	public Vector3 set(Vector3 v) {
		return set(v.x, v.y, v.z);
	}
	
	public Vector3 scale(float s) {
		this.x *= s;
		this.y *= s;
		this.z *= s;
		return this;
	}
	
	public float length() {
		return (float) Math.sqrt(x*x + y*y + z*z);
	}
	
	public static float dot(Vector3 v1, Vector3 v2) {
		return v1.x*v2.x + v1.y*v2.y + v1.z*v2.z;
	}
	
	public static float angle(Vector3 v1, Vector3 v2) {
		return (float)Math.acos( dot(v1, v2) / (v1.length()*v2.length()) );
	}
}
