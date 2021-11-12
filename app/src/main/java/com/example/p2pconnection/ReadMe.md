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

*** In the code DeviceListFragment:
1. The code uses property access syntax - for settext and setvisibility 
2. The layout variable includes a suppress resource type - what is the functionality and need
   (Must be changed in case of errors in progressbar)

*** In the code DeviceDetailFragment
1. In this and the previous one instead of onActivityCreated, onViewCreated is used -- check the timeline
and verify that it is fine
2. What is suppress SetTestI18n compression and why must it be done. In setUri
3. A part of the onClick is not completed and requires the completion of FileTransfer

*** In the Main Activity code:
1. manager and fragment cannot be null. This can be changed if required.

*** In the Listener code:
1. the else part of isConnected is not added -- is this required

** In XML files:
1. Inside menu action_items the images must be changed (all are kept to machine to check the working)
