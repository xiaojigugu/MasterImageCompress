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
	        implementation 'com.github.xiaojigugu:MasterImageCompress:1.0.2'
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
             	//是否保留源文件
                .keepSource(false)
             	//压缩方式，分为质量压缩、像素压缩、质量压缩+像素压缩，慎用单独的质量压缩（很容易OOM）！
                .comPressType(CompressConfig.TYPE_PIXEL_AND_QUALITY)
                //目标长边像素,启用像素压缩有效（eg：原图分辨率:7952 X 5304，压缩后7952最终会小于1280）
                .maxPixel(1280)
                //目标大小200kb以内，启用质量压缩有效
                .targetSize(200 * 1024)
            	//压缩格式
                .format(Bitmap.CompressFormat.WEBP, Bitmap.Config.ARGB_8888) 
            	//输出目录,AndroidQ仅能输出到私有目录
                .outputDir(getFilesDir().getAbsolutePath() + File.separator + "image_compressed/") 
                .build();
     
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



4. **新增文件管理类用于适配AndroidQ**

```java
//结合Rxjava将选中的图片拷贝至APP私有目录以方便进行压缩操作
    private void moveImages(final List<Uri> uris) {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> emitter) {
                Log.i(TAG, "ObservableOnSubscribe: thread-->" + Thread.currentThread().getName());
                emitter.onNext(FileUtils.copyImagesToPrivateDir(getApplicationContext(), uris));
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<String>>() {
                    @Override
                    public void accept(List<String> objects) {
                        Log.i(TAG, "subscribe: thread-->" + Thread.currentThread().getName());
                        //开始压缩
                        compress(objects);
                    }
                });
    }
```
