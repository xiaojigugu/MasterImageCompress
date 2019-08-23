# MasterImageCompressor
**线程池+队列+观察者模式图片压缩框架**

[![](https://jitpack.io/v/xiaojigugu/MasterImageCompress.svg)](https://jitpack.io/#xiaojigugu/MasterImageCompress)  

#### 使用

1. **Add it in your root build.gradle at the end of repositories:**
``` gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```  

2.  **Add the dependency:**
```  gradle
	dependencies {
	        implementation 'com.github.xiaojigugu:MasterImageCompress:1.0.1'
	}
```  

3. **start**  
``` xml
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```


```  java
            //配置压缩条件
     CompressConfig compressConfig = CompressConfig
           .builder()
           .keepSource(true) //是否保留源文件
           //压缩方式，分为TYPE_QUALITY、TYPE_PIXEL、TYPE_PIXEL_AND_QUALITY，慎用单独的TYPE_QUALITY（很容易OOM）！
           .comPressType(CompressConfig.TYPE_PIXEL)
           //目标长边像素,对TYPE_PIXEL有效（eg：原图分辨率:7952 X 5304，压缩后7952最终会小于2000）
           .maxPixel(1280)
           //目标大小500kb以内，对TYPE_QUALITY有效
           .targetSize(200 * 1024)
           .format(Bitmap.CompressFormat.WEBP, Bitmap.Config.ARGB_8888) //压缩配置
           .outputDir("storage/emulated/0/DCIM/image_compressed/") //输出目录
           .build();
             
     //或者一句话CompressConfig compressConfig=CompressConfig.getDefault();
                   
     //添加需要压缩的图片路径       
     List<String> images = new ArrayList<>();
     for (File file1 : files) {
         String path = file1.getAbsolutePath();
         SystemOut.println("ImageCompressor ===> image,path=" + path);
         images.add(path);
     }
                    
     ImageCompressManager.builder()
           .paths(images)
           .config(compressConfig)
           .listener(new ImageCompressListener() {
                @Override
                public void onStart() {
                     SystemOut.println("ImageCompressor ===>开始压缩");
                }

                @Override
                public void onSuccess(List<ImageInstance> images) {
                     SystemOut.println("ImageCompressor ===>压缩成功");
                }

                @Override
                public void onFail(boolean allOrSingle, List<ImageInstance> images, CompressException e) {
                      SystemOut.println("ImageCompressor ===>压缩失败，isAll=" + allOrSingle);
                }
           })
           .compress();
```  
