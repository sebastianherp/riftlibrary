package net.appsdoneright.riftlib.util;

public class Vector3 {
	public float x,y,z;
	
	public Vector3() {
		x = y = z = 0;
	}
	
	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3 scale(float s) {
		this.x *= s;
		this.y *= s;
		this.z *= s;
		return this;
	}
}
