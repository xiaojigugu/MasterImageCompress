# SuperImageCompressor
**线程池+队列+观察者模式图片压缩框架**

[![](https://jitpack.io/v/xiaojigugu/SuperImageCompressor.svg)](https://jitpack.io/#xiaojigugu/SuperImageCompressor)  

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
	        implementation 'com.github.xiaojigugu:SuperImageCompressor:1.0.0'
	}
```  

3. **start**
```  java
            //配置压缩条件
            CompressConfig compressConfig = CompressConfig
                    .builder()
                    .keepSource(false) //是否保留源文件  
                     //压缩方式，分为质量压缩、像素压缩、质量压缩+像素压缩，慎用单独的质量压缩（很容易OOM）！
                    .comPressType(CompressConfig.TYPE_PIXEL_AND_QUALITY)
                    .maxPixel(2000)  //目标长边像素（eg：原图分辨率:7952 X 5304，压缩后7952最终会小于2000）
                    .targetSize(500 * 1024) //目标大小500kb以内
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
