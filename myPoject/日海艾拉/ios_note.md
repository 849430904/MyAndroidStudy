* 日志修改：

````
  ALLogging.h
  DLog

````

* 项目启动

````
  1，加载CTLogin.storyboard中的CTLogInViewController
  2,CTLogInViewController.CTLoginModelDelegate.didInitializeSession
  3,signIn //自动登陆
  4，CTLoginModel.loginWithSuccess
  5,CTLogInViewController.didLogInWithSessionManager //登陆成功
  6，push CTMenuManagerViewController 
       CTDrawer.storyboard

````


* 设备控制：

````
  CTEVBDeviceCollectionViewCell.switchBlueLEDtoState
   =>EVBDevice.setBlueLEDOn
   =>EVBDevice.setPropertyWithName
      =>AylaProperty.AMAP_createDatapoint
      =>AylaProperty.createDatapoint

````