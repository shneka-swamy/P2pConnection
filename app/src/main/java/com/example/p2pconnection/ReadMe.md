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

*** General:
1. What is the difference between ? and !! in null supression
2. What is the use of getString in getResources and how does it get values? 
3. Understand where to cast the value and where the value must be initialised
4. What is suppressing null type?
5. What is wps setup and how is the info derived
6. What is the functionality of yield

*** In the code DeviceListFragment: 
2. The layout variable includes a suppress resource type - what is the functionality and need
   (Must be changed in case of errors in progressbar)

*** In the code DeviceDetailFragment
2. What is suppress SetTestI18n compression and why must it be done. In setUri

*** In the Listener code:
1. the else part of isConnected is not added -- is this required

** In XML files:
1. Inside menu action_items the images must be changed (all are kept to machine to check the working)
