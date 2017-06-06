# atlasDemo
## 概念
atlas:容器化框架，提供组件化开发，热更新。

![image](http://atlas.taobao.org/docs/principle-intro/Project_architectured_img/runtime_struct.png)

如上图所示，atlas主要分为以下几个层级：

1. 最底下的hack工具层： 包括了容器所需的所有系统层面的注入和hack的工具类初始化和校验，容器启动时先校验设备是否支持容器运行，不支持则采取降级并记录原因；
2. Bundle Framework 负责bundle的安装 更新 操作以及管理整个bundle的生命周期；
3. runtime层：主要包括清单管理、版本管理、以及系统代理三大块，基于不同的触发点按需执行bundle的安装和加载；runtime层同时提供了开发期快速debug支持和监控两个功能模块。从Delegate层可以看到，最核心的两个代理点：一个是DelegateClassLoader：负责路由class加载到各个bundle内部，第二个是DelegateResource：负责资源查找时能够找到bundle内的资源；这是bundle能够真正运行起来的根本；其余的代理点均是为了保证在必要的时机按需加载起来目标bundle，让其可以被DelegateClassloader和DelegateResource使用
4. 对外接入层:AtlasBridgeApplication是atlas框架下apk的真正Application，在基于Atlas框架构建的过程中会替换原有manifest中的application，所以Atlas没入的接入并不存在任何初始化代码，构建脚本完成了接入的过程。AtlasBridgeApplication里面除了完成了Atlas的初始化功能，同时内置了multidex的功能，这样做的原因有两个：

很多大型的app不合理的初始化导致用multidex分包逻辑拆分的时候主dex的代码就有可能方法数超过65536，AtlasBridgeApplication与业务代码完全解耦，所以拆分上面只要保证atlas框架在主dex，其他代码无论怎么拆分都不会有问题；
如果不替换Application，那么atlas的初始化就会在application里面，由于基于Atlas的动态部署实际上是类替换的机制，那么这种机制就会必然存在包括Application及其import的class等部分代码在dalvik不支持部署的情况，这个在使用过程中造成一定成本，需要小心的使用以避免dalivk内部class resolve机制导致部分class没成功，替换以后该问题得到最好的解决，除atlas本身以外，所有业务代码均可以动态部署；
另外内置的原生的multidex在dalvik上面性能并不好，atlas内部对其进行了优化提高了在dalvik上面的体验。

除AtlasBridgeApplication之外，接入层对外提供了部分工具类，包括主动install bundle，start bundle，以及获取全局的application等各种功能。

## 集成
1. 工程项目的build.gradle文件依赖插件：
![image](http://opy4iwqsf.bkt.clouddn.com/WX20170606-172140@2x.png)

2. app目录下的build.gradle使用插件，并且添加相关依赖
![image](http://opy4iwqsf.bkt.clouddn.com/WX20170606-172517@2x.png)
![image](http://opy4iwqsf.bkt.clouddn.com/WX20170606-172753@2x.png)
![image](http://opy4iwqsf.bkt.clouddn.com/WX20170606-172140@2x.png)

3. 加载自启动的bundle
  
```
switchToActivity("home","com.taobao.firstbundle.FirstBundleActivity");
//通过类名来调用
 public void switchToActivity(String key,String activityName){
        Intent intent = new Intent();
        intent.setClassName(getBaseContext(),activityName);
        activityGroupDelegate.startChildActivity(framelayout,key,intent);
    }
```
![image](http://opy4iwqsf.bkt.clouddn.com/2017-06-06%2017.48.21.gif)
可以看到现在的fistBundle中弹出了一个toast,message 为“更新一下”,下面修改firstBundle中的代码，然后热更新一下

修改toast的message
```
@Override
    protected void onResume() {
        super.onResume();
        Toast.makeText(this, "单模块部署1111111", Toast.LENGTH_SHORT).show();
    }
```

bundle热更新步骤
1、 app的build.gradle的语句"version = getEnvValue("versionName", "1.0.0");"中修改想要生成的app的versionName（默认为1.0.0）

    app目录下执行../gradlew clean assembleDebug publish

    (生成apk同时将跟apk同目录的ap文件发布到仓库)

2、 手机上安装apk，同时进到动态部署界面（侧边栏里面划开点击进入),且手机连接电脑adb（确保adb devices可见）

///////////////////////////////^^^^^^^准备工作^^^^^^^^^^////////////////////////

3、 进行一些想要的修改（暂时不支持manifest的修改，会在近期上线）

4、 app工程目录下执行../gradlew clean assembleDebug -DapVersion=apVersion -DversionName=newVersion,
    其中apVersion为之前打的完整apk的版本，newVersion为此次动态部署要生成的新的版本号

5、 检查build/output/tpatch-debug 目录下文件是否生成，然后执行下面的命令(以下为mac下的命令，windows请修改文件分隔符)
  
```
adb push build/outputs/tpatch-debug/update.json /sdcard/Android/data/cx.com.atlasdemo11111/cache/update.json
adb push build/outputs/tpatch-debug/patch-*.tpatch /sdcard/Android/data/cx.com.atlasdemo11111/cache
```


6、 点击动态部署页面红色按钮执行动态部署

更改firstBundle中的代码，然后
在app目录下执行命令，app/build/outputs/  会出现
```
//DapVersion基准版本号      DversionName更新版本号
../gradlew clean assembleDebug -DapVersion=1.0.0 -DversionName=1.0.1

```
命令跑完后就会出现下面的目录结构

![image](http://opy4iwqsf.bkt.clouddn.com/WX20170606-174204@2x.png)

执行

```
adb push build/outputs/tpatch-debug/update.json /sdcard/Android/data/cx.com.atlasdemo11111/cache/update.json
adb push build/outputs/tpatch-debug/patch-*.tpatch /sdcard/Android/data/cx.com.atlasdemo11111/cache
```
然后点击动态部署按钮,效果如下

![image](http://opy4iwqsf.bkt.clouddn.com/2017-06-06%2017.55.31.gif)

可以看到热更新已经成功。

### 远程bundle使用
1. 远程bundle必须在gradle中配置，如上图

```
atlas {

    atlasEnabled true
    tBuildConfig {
        autoStartBundles = ['com.taobao.firstbundle'] //自启动bundle配置
        outOfApkBundles = ['remotebundle']//配置远程bundle
    }

    manifestOptions{
        addAtlasProxyComponents true
    }


    patchConfigs {
        debug {
            createTPatch true
        }
    }


    buildTypes {
        debug {
            if (apVersion) {
                baseApDependency "com.taobao.android.atlasdemo:AP-debug:${apVersion}@ap"
                patchConfig patchConfigs.debug
            }
        }
    }
}
```
2.在Application中添加安装远程bundle的回调

```
Atlas.getInstance().setClassNotFoundInterceptorCallback(new ClassNotFoundInterceptorCallback() {
            @Override
            public Intent returnIntent(Intent intent) {
                final String className = intent.getComponent().getClassName();
                final String bundleName = AtlasBundleInfoManager.instance().getBundleForComponet(className);

                if (!TextUtils.isEmpty(bundleName) && !AtlasBundleInfoManager.instance().isInternalBundle(bundleName)) {

                    //远程bundle
                    Activity activity = ActivityTaskMgr.getInstance().peekTopActivity();
                    File remoteBundleFile = new File(activity.getExternalCacheDir(),"lib" + bundleName.replace(".","_") + ".so");

                    String path = "";
                    if (remoteBundleFile.exists()){
                        path = remoteBundleFile.getAbsolutePath();
                    }else {
                        Toast.makeText(activity, " 远程bundle不存在，请确定 : " + remoteBundleFile.getAbsolutePath() , Toast.LENGTH_LONG).show();
                        return intent;
                    }


                    PackageInfo info = activity.getPackageManager().getPackageArchiveInfo(path, 0);
                    try {
                        Atlas.getInstance().installBundle(info.packageName, new File(path));
                    } catch (BundleException e) {
                        Toast.makeText(activity, " 远程bundle 安装失败，" + e.getMessage() , Toast.LENGTH_LONG).show();

                        e.printStackTrace();
                    }

                    activity.startActivities(new Intent[]{intent});

                }

                return intent;
            }
        });
```
3.把app/build/outputs/remote-bundles-debug/libcom_taobao_remotebundle.so 放到sd卡的cache目录，执行命令

```
adb push build/outputs/remote-bundles-debug/libcom_taobao_remotebunle.so /sdcard/Android/data/cx.com.atlasdemo11111/cache/libcom_taobao_remotebunle.so

```
点击添加远程bundle看效果
![image](http://opy4iwqsf.bkt.clouddn.com/2017-06-06%2018.06.42.gif)
可以看到已经将远程bundle加载成功。

在自己集成的demo中也有很多的坑。跑命令失败的情况。记录一下
## 集成atlas时的坑
1. app工程目录下执行../gradlew clean assembleDebug -DapVersion=apVersion -DversionName=newVersion

这个命令中的apVersion必须是存在的版本号，比如在gradle中设置的1.0.0，不然会报找不到依赖
2.
```
android {
    compileSdkVersion 25
    buildToolsVersion "25.0.3"
    defaultConfig {
        applicationId "cx.com.atlasdemo11111"
        minSdkVersion 15
        targetSdkVersion 25
        versionCode 1
        versionName version //这里的versionName要和脚本中的一致，如果设置死，在热修复merge的时候不会起作用
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}
```
3.这个版本的单模块调试模拟还不稳定

atlas不得不说是良心之作，这个demo只是简单地集成，以供参考。

[点击下载代码](https://github.com/xingege662/atlasDemo)
