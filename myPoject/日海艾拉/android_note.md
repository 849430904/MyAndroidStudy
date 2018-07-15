* 修改LOG的输出：AylaLog
* Android登陆  SignInActivity

````

//登陆流程：
MainActivity.onResume
 =>	MainActivity.checkLoginAndConnectivity
	 =>MainActivity.showLoginDialog
	   =>MainActivity.signIn(String username, String password)


SignInActivity._loginButton.setOnClickListener ...
=>AMAPCore.startSession
   =>AMAPCore.start
     =>AylaNetworks.signIn 
       =>SignInActivity.successListener
         =>SignInActivity.setResult(Activity.RESULT_OK);  

 最终会调用AMAPCore的start方法进行登陆                   
````

* 绑定设备：
* 设备控制：AllDevicesFragment 、AylaEVBDevice

````
 MainActivity中点击左边item的时候:
 
_navigationView.setNavigationItemSelectedListener(
        new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                closeDrawer();
                MenuHandler.handleMenuItem(menuItem);
                return true;
            }
        });
 
  数据源绑定：
   AllDevicesFragment.onCreateView
     =>AllDevicesFragment.updateDeviceList (_recyclerView.setAdapter(_adapter))
       =>DeviceListAdapter.fromDeviceList
         =>ViewModelProvider.viewModelForDevice
           =>new ViewModel(aylaDevice) //至此，就将设备与viewMode绑定在一起了，只要点击viewMode就能获取到设备
             =>new DeviceListAdapter(viewModels, listener)//将viewModels绑定到了recyclerView的数据源
   
   不同的设备显示不同的内容：
   DeviceListAdapter.onCreateViewHolder
     =>AMAPViewModelProvider.viewHolderForViewType//返回不同的设备，如plug、switch;
        AMAPViewModelProvider.viewModelForDevice会自动包装成不同类型的设备
   
   单击的时候如何控制：
   AylaEVBDevice.bindViewHolder //注意它的父类
     =>setOnClickListener //将按钮绑定单击事件
       =>setBlueLED // 单击时
         =>ViewModel.setDatapoint
          => _device.getProperty //获取要控制的属性（因为设置数据源的时候，已经绑定了设备）
            =>AylaProperty.createDatapoint
              =>AylaProperty.createDatapointLAN //局域网
              =>AylaProperty.createDatapointCloud //云 
                 =>AylaDeviceManager.sendDeviceServiceRequest(request);
                   =>deviceRequestQueue.add(request);//将消息添加到队列 调用volley框架发出的请求
      
             
    
 局域网通信：
  AylaProperty.createDatapointLAN //局域网 
    =>AylaDevice.CreateDatapointCommand //创建packet
      =>AylaLanModule.sendRequest
         => NetThread：负责Socket(UDP)
````


* 定时器设置：DeviceDetailFragment(设备详情) =>ScheduleFragment(定时设置)

````
 //定时器的保存
 ScheduleFragment.saveSchedule
   =>AylaDevice.updateSchedule
     //将对象转JSON消息发出去
     =>AylaNetworks.sharedInstance().getGson().toJson
                (scheduleWrapper, AylaSchedule.Wrapper.class);
````

* 分享设备：DeviceDetailFragment=> ShareDevicesFragment

````
ShareDevicesFragment.shareDevices
  =>ShareDevicesListener.shareDevices
     => DeviceDetailFragment.shareDevices
       =>AylaSessionManager.createShare//会将share对象转json,然后发送出去

````

* 解除绑定设备：DeviceDetailFragment

````
DeviceDetailFragment.onOptionsItemSelected
  =>DeviceDetailFragment.unregisterDevice
    =>DeviceDetailFragment.doUnregisterDevice
      =>AylaDevice.unregister  Request.Method.DELETE 

````

* 扫描：AddDeviceFragment

````
 扫描：
 AddDeviceFragment.scanButtonClick
   =>AddDeviceFragment.doScan
     =>AylaSetup.scanForAccessPoints
       =>WifiManager.startScan
         =>AylaSetup.ScanReceiver.onReceive
           =>AddDeviceFragment.doScan.scanForAccessPoints.apply 扫描结果通过Ayla-xx来判断
````

* 配置设备：AddDeviceFragment

````
 =>AddDeviceFragment.connectToDeviceAP 当选择SSID或设备开始连接的时候
   =>AylaSetup.startDeviceScanForAccessPoints
     =>AddDeviceFragment.connectToNewDevice.onResponse
       =>AddDeviceFragment.deviceScanForNetworks
         =>AddDeviceFragment.fetchDeviceScannedNetworks
           =>AddDeviceFragment.choseAccessPoint
             =>AddDeviceFragment.connectDeviceToService
              =>AylaSetup.connectDeviceToService
                =>connectMobileToOriginalNetworkAndConfirmDeviceConnection连接云确认
                  =>confirmDeviceConnection
                    =>具体执行哪个方法要看选择的模式
````

* 群组管理 DeviceGroupsFragment

````
//添加组
 DeviceGroupsFragment.onAddGroup  
   =>GroupManager.createGroup 
     =>GroupManager.pushGroupList
      =>DeviceGroupsFragment.onAddDeviceToGroup
        =>DeviceGroup.setDevices
        => DeviceGroup.pushToServer
        
        
````

-------
##### Android界面
------

* 首先菜单点击

````
 点击左边菜单的时候：参考：https://blog.csdn.net/wangwangli6/article/details/70210858
 _navigationView.setNavigationItemSelectedListener
  =>MenuHandler.handleMenuItem(menuItem);
     =>MenuHandler.handleMenuId
       =>MenuHandler.replaceFragmentToRoot(XXXXXFragment.newInstance());
 
````
