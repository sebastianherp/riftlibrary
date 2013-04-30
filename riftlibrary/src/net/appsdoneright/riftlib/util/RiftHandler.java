package net.appsdoneright.riftlib.util;

public interface RiftHandler {
	void onDataReceived(Quaternion q, int frequency);
	void onKeepAlive(boolean result);
}
