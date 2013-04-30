package net.appsdoneright.riftlib.util;

public interface RiftHandler {
	void onDataReceived(Quaternion q);
	void onKeepAlive(boolean result);
}
