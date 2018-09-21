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
       AylaLanRequest => _lanCommands.add(lanCommand); //AylaLanRequest设置了URL="local"
      =>AylaLanModule.sendRequest
        => AylaLanModule.sendRequest._lanRequestQueue
       
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


* 设备列表：AllDevicesFragment
	* 插座设备： OutletDevice 
* 添加设备：AddOutletDeviceFragment
* 添加设备流程：

````
  1,扫描网络 _aylaSetup.scanForAccessPoints
  2，扫描设备 _aylaSetup.scanForAccessPoints
  3，连接设备 _aylaSetup.connectToNewDevice
  4，设备配网  _aylaSetup.connectDeviceToService
             _aylaSetup.reconnectToOriginalNetwork
  5，绑定设备 _aylaSetup.confirmDeviceConnected
````
* 设备详情 OutletDetailFragment

````
   查询倒计时：_deviceModel.getDevice().fetchSchedules
   开关： _deviceModel.setDatapoint
   倒计时：
      _deviceModel.getDevice().updateSchedule
      _deviceModel.getDevice().createSchedule
````

* 定时器列表：TimingListFragment  TimingListAdapter

  
````
   定时器获取：deviceModel.getDevice().fetchSchedules
````
* 定时器添加：OutletTimingFragment

````
   _deviceModel.getDevice().updateSchedule 更新
   _deviceModel.getDevice().createSchedule 添加
````

* MoreFragment 更多

````
   _deviceModel.getDevice().updateSchedule 更新
   _deviceModel.getDevice().createSchedule 添加
   删除设备：deviceModel.getDevice().unregister
   恢复出厂设置：deviceModel.getDevice().deleteSchedule 实际上清空所有schedule
````

* 分享：DeviceShareFragment

````
  authProvider.fetchUserProfileWihPhone 账号校验
  AMAPCore.sharedInstance().getSessionManager().createShare
````

* 我的：MeFragment

````
   退出登陆：sessionManager.shutDown
````
* 场景：SceneFragment
* 修改密码:ChangePasswordFragment

````
  AMAPCore.sharedInstance().getSessionManager().updatePassword //邮箱方式修改密码
  phoneServiceAuthProvider.updatePassword 手机方式修改密码
````

* 输入或修改Key:AuraConfigEditor
  
* 底部菜单 

````
   MainActivity.initBottomNavigation
   activity_main_bottomnavigation
   menu_bottom_navigation
  
   
````
 
83740372   YFceshi:belon83740860      zhuanyongtest
2000@qq.com

##### 问题
* 连接设备失败：如果app连接到设备，而app又提示连接失败，这种情况无论如何操作，都不会成功；
* 有时候会绑定失败
* 三星配网问题。在三星Galaxy Note3、Android5.0上面就经常出现连接失败，原因如下：
* 有时候反复的点击，会出现设备状态与app状态不一致情况
* 有时候会出现控制不了情况（很少出现）

````

 => AylaSetup.connectToNewDevice
   //已经连接了，会直接进行
   =>AylaSetup.fetchDeviceDetail  http://192.168.0.1/status.json  GET  连不通
   因为这时网络状态还不可用，会导致http请求失败
  
````

* 新版的设备列表界面：cardview_outlet_list

* ViewModel里面的device是怎么实时更新的？

````
AllDevicesFragment 监听 DeviceChangeListener DeviceManagerListener


AylaDSManager 负责websocket通信
AylaDeviceManager  轮询
````

* 手机HTTP Server

````
  设备端和app端都有自己的httpServer,他们通过云端获取自己的IP
  
  ayla_reset
  ayla_conf show
  ayla_reset factory

  固定mac地址：txevm -e 2
  
  bk7231-id
  bk7231-PiWlHt_pCVgSBc_75ac-47RBnEE
  
  
  修改debug日志等级：
    1，ayla_log all
    2，ayla_conf save
  
    SC000W000018511
  
````

https://dashboard-dev.sunseaiot.com/sessions/new
https://dashboard-dev.sunseaiot.com/sessions/new#/devices/SC000017033


https://ads-dev.sunseaiot.com/apiv1/dsns/SC000W000017103/properties.json


http://192.168.0.1/status.json  GET

* [7色环](https://github.com/xuhongv/ColorSeerBarPicker)
* [顶部滑动](https://github.com/lyxRobert/Android-AppBarLayout-TabLayout-RecyclerView-ViewPager-Fragment)
* [颜色渐变](https://blog.csdn.net/archer_zoro/article/details/37671171)
* [动态修改Gradient](https://www.cnblogs.com/popfisher/p/5606690.html)


* [ios七色环拾色](https://github.com/WangMing1998/WMColorPicker)
* * [ios七色环拾色](https://github.com/lyleLH/ColorPicker)


* 通信流程：

````

   通信有三种模式：
      Lan mode 不同步：app发消息通知设备端，设备端通过https向手机端获取指令，app返回指令。设备端执行，执行完将指令同步到云。
      Lan mode 同步：app发消息通知设备端，设备端通过https向手机端获取指令，app返回指令。
      iColud模式：APP发指令到云，云通过UDP通知设备端来获取指令
      
      Lanmode是否需要同步可以在模板里面设置
      是否启用lanmode也可以在模板里面设置
      
      
  TAG = LanModule 设备端来APP获取指令   
  
  LanModule：APP定时向设备端发起心跳（通过LanModule.sendLocalRegistration方法）
            APP向设备端通过HTTP发送:{"local_reg":{"ip":"172.16.20.109","uri":"/local_lan","notify":0,"port":10275}}， notify如果为1，代表设备端需要向自己来取指令； ip表示手机的IP,port表示手机对应的端口号
  
  
  本地通信流程：
     1，APP通过HTTPS定时向设备端发送local_reg命令，如果用户有操作，就将local_reg中的notify设置1；
     2,设备端检测到notify为1的时候，通过https向APP索取指令执行
     3，设备端执行完以后，通过websocket将属性广播给app
     
     APP到云端查询设备端的IP,设备端通过APP 的local_reg来获取APP的端口号与IP
     
  远程通信设备端：
    1，notify: recv: op 9 notify
    
    
    notify: send: sending seq 56e op 7 keep-alive 
    


   本地通信流程：
     1，发送Request到本地http server
        _aylaLocalNetwork = new AylaLocalNetwork(new BasicNetwork(new HurlStack()));
        _lanRequestQueue = new RequestQueue(cache, _aylaLocalNetwork);//指明由AylaLocalNetwork处理
     2,发送notify到设备端
     3，设置过来取指令

	nslookup smartplug-c46f5ac9-device.sunseaiot.com
	Server:		202.96.134.133
	Address:	202.96.134.133#53
	
	Non-authoritative answer:
	smartplug-c46f5ac9-device.sunseaiot.com	canonical name = ads-dev.sunseaiot.com.
	ads-dev.sunseaiot.com	canonical name = edge-ads-ssct-alb-149096517.cn-north-1.elb.amazonaws.com.cn.
	Name:	edge-ads-ssct-alb-149096517.cn-north-1.elb.amazonaws.com.cn
	Address: 52.80.142.112
	Name:	edge-ads-ssct-alb-149096517.cn-north-1.elb.amazonaws.com.cn
	Address: 52.80.21.153


````


