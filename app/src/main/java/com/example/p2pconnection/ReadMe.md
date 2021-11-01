This project aims in generating a P2P connection in which two devices communicate with each other to 
send 360 degree video from one device to another.

The materials for all preliminary functions are got from

1. A guide to set up leader and group formation


** The code base to set up the initial file transfer is done using this code
** Modifications are made to this code to improve the functionality.
2. General identification and connection
   https://developer.android.com/guide/topics/connectivity/wifip2p#broadcast-receiver


*** Where to put this code?
val device = peers[0]
val config = WifiP2pConfig().apply {
deviceAddress = device.deviceAddress
wps.setup = WpsInfo.PBC
}

** The code uses property access syntax - for settext and setvisibility (If something goes wrong try 
reverting to this segment of the code)