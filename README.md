Oculus Rift support for Android apps
====================================

This libary is currently a side project and supports reading sensor data from
the [Oculus Rift](http://www.oculusvr.com/)

Setup
-----

To use the library in your application you need to modify your activity in `AndroidManifest.xml` and add the following intent filter:

	<intent-filter>
		<action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
    </intent-filter>
	<meta-data android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
		android:resource="@xml/device_filter" />
		
I also recommend setting `android:launchMode="singleTask"`

As the libary currently sets `android:minSdkVersion="12"`so should your application.

Usage
-----

TBD