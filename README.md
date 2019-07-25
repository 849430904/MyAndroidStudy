##### [Android基础知识、Android进阶知识、Android自定义View相关、面试相关的知识](https://github.com/linsir6/AndroidNote)

````
  git clone https://github.com/linsir6/AndroidNote.git
````
------
##### [itheima1收集Android方方面面的经典知识](https://github.com/itheima1/Android)
````
  git clone https://github.com/itheima1/Android.git
````

------
##### [GitHub上最火的Android开源项目,所有开源项目都有详细资料和配套视频](https://github.com/open-android/Android)
````
  git clone https://github.com/open-android/Android.git
````

* [Android反编译](https://github.com/onlybeyond/crack)

* ConstraintLayout  [参考郭神](https://blog.csdn.net/guolin_blog/article/details/53122387) [参考2](https://blog.csdn.net/c10wtiybq1ye3/article/details/78098515)  [参考3](https://blog.csdn.net/u012538536/article/details/65042525)

````

layout_constraintLeft_toLeftOf
layout_constraintLeft_toRightOf
layout_constraintRight_toLeftOf
layout_constraintRight_toRightOf
layout_constraintTop_toTopOf
layout_constraintTop_toBottomOf
layout_constraintBottom_toTopOf layout_constraintBottom_toBottomOf
layout_constraintBaseline_toBaselineOf
layout_constraintStart_toEndOf
layout_constraintStart_toStartOf
layout_constraintEnd_toStartOf
layout_constraintEnd_toEndOf
    属性都形如 layout_constraintXXX_toYYYOf,这里我的理解，constraintXXX 里的 XXX 代表是这个子控件自身的哪条边(Left、Right、Top、Bottom、Baseline)， 
    而 toYYYOf 里的 YYY 代表的是和约束控件的 哪条边 发生约束 (取值同样是 Left、Right、Top、Bottom、Baseline)。
    当 XXX 和 YYY 相反时，表示控件自身的 XXX 在约束控件的 YYY 的一侧，例如 app:layout_constraintLeft_toRightOf="@id/button1" ，表示的是控件自身的左侧在 button1 的右侧。    当 XXX 和 YYY 相同时，表示控件自身的 XXX 和约束控件的 YYY 的一侧 对齐，例如：app:layout_constraintBottom_toBottomOf="parent"，表示控件自身底端和父控件底端对齐
````


* 项目部份：

````
 app异常统计：https://bugly.qq.com/docs/user-guide/instruction-manual-android/?v=20180709165613
 APP卡顿分析工具：
    https://github.com/markzhai/AndroidPerformanceMonitor/blob/master/README_CN.md
    https://www.jianshu.com/p/e58992439793
    
````

* [微信公众号「郭霖」「鸿洋」「玉刚说」历史文章的索引](https://github.com/zhuanghongji/mp-android-index)
* [鸿洋视频](https://www.imooc.com/u/320852/courses)
* [流式标签布局](https://blog.csdn.net/qq_33923079/article/details/53700556)  [流式布局2](https://github.com/ykayyou/Android_FlowLayout)

* [Volley源码解析系列](https://blog.csdn.net/guolin_blog/article/details/17482165)

* [Android引蒙层 
新手引导视图](https://github.com/qiushi123/GuideView-master)

* [Android FPS 内存监控](https://github.com/lyric315/UIPerforance)


------

# 开发常用框架
* [de.hdodenhof:circleimageview:2.2.0 圆角图片处理](https://github.com/hdodenhof/CircleImageView)
* [uCrop图片裁剪demo](https://github.com/asia-meidia/UcropDemo-master)
* [com.bigkoo:svprogresshud:1.0.6](https://github.com/Bigkoo/Android-SVProgressHUD)


#### [adb常用命令](https://github.com/mzlogin/awesome-adb)

````
adb命令 : adb shell am start -S -W 包名/启动类的全限定名 ， -S 表示重启当前应用

````