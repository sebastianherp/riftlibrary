package net.appsdoneright.riftlib.util;

public interface RiftHandler {
	void onDataReceived(byte[] buffer, int length);
	void onKeepAlive(boolean result);
}
