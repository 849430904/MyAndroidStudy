* 日志修改：

````
  ALLogging.h
  DLog

````

* 核心类：

````
  AylaSessionManager：Manages data and objects associated with the currently authenticated session
  AylaUser：store username、password
  AylaDatum：key->value
  AylaContact:Represents a contact to be used with `AylaDeviceNotificationApp` or `AylaPropertyTriggerApp`
  AylaDevice：
     fetchPropertiesXXX:获取设备状态
  AylaProperty：描述设备属性   
  AylaShare:dev share
  AylaDeviceManager：管理所有设备
  AylaSetup：

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

  左边界面的菜单的配置见：
     ThemeSettings.mainControllerWithSession
     ThemeSettings.configureViewControllersWithSession
     
 CTMenuTableViewController:左侧菜单控制器
 DashboardCollectionViewController：设备控制列表    
````


* 设备控制：

````
  CTEVBDeviceCollectionViewCell.switchBlueLEDtoState
   =>EVBDevice.setBlueLEDOn
   =>EVBDevice.setPropertyWithName
      =>AylaProperty.AMAP_createDatapoint
      =>AylaProperty.createDatapoint

````

* 设备详情：

````
  AylaDeviceViewModel.displayDeviceDetailsFromController
    =>push CTGenericDeviceDetailsViewController
       

````


* [Reaveal 破解](https://www.jianshu.com/p/27f1951d6004)