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

As the libary currently sets `android:minSdkVersion="12"` so should your application.

Usage
-----

TBD

Debugging
---------

Most devices will only have a single USB port so you can't debug over USB and use the Rift at the same time. Luckily you can switch to 
TCP/IP with the following commands while using USB connection(replace IP with your device IP):
	
	adb tcpip 5555
	adb connect 192.168.1.2:5555
	
You can now connect the Rift instead and still use Eclipse to push new APKs and debug the app.

Switch back to USB with `adb usb`