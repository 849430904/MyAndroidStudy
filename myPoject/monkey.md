* monkey:是一个稳定性与压力测试的命令行工具
* 简介：monkey是通过adb命令启动的

````
  1,adb shell
  2,cd /system/framework 
  3,查看monkey路径：ls | grep monkey
  
  
  4，cd /system/bin
  5, ls | grep monkey  //查看monkey脚本
  5，cat monkey  //查看monkey启动脚本
  
  monkey就像一只猴子一样，不停的乱点界面
  
  例如：
   adb shell monkey -p com.beleon.amap -v 1000
````
* monkey环境变量的配置：其实就是配置adb
* monkey如何运行：
	* 直接PC启动:adb shell monkey [option] <count> , option可选参数 ，count必选
	* shell端启动
		* adb shell
		* monkey [option] <count>

* 保存运行Log	

````
  保存在PC中， adb shell monkey [option] <count> > d:\monkey.txt
  保存在手机中： monkey [option] <count> > /mnt/sdcard/monkey.txt
  标准流与错误流分开保存：
   monkey [option] <count> 1>/mnt/sdcard/monkey.txt 2>/mnt/sdcard/error.txt
````

* [monkey脚本参考](https://github.com/fan2597/MonkeyCreateScript) [代码讲解](https://www.jikexueyuan.com/course/2438_2.html?ss=1)