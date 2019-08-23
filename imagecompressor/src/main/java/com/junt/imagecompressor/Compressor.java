package com.junt.imagecompressor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;

import com.junt.imagecompressor.bean.ImageInstance;
import com.junt.imagecompressor.config.CompressConfig;
import com.junt.imagecompressor.exception.CompressException;
import com.junt.imagecompressor.listener.ImageCompressListener;
import com.junt.imagecompressor.util.SystemOut;
import com.junt.imagecompressor.util.ThreadPoolManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * 图片压缩类
 */
public class Compressor implements Observer {
    private Handler handler = new Handler();
    //压缩了第几张图片
    private int compressIndex;

    @Override
    public void update(Observable observable, Object o) {
        ImageCompressManager imageCompressManager = (ImageCompressManager) o;
        compress(imageCompressManager.getCompressConfig(), imageCompressManager.getImageInstanceList(), imageCompressManager.getImageCompressListener());
    }

    private void compress(final CompressConfig compressConfig, final List<ImageInstance> imageInstances, final ImageCompressListener imageCompressListener) {
        if (!checkOutPutPath(compressConfig.getOutPutPath())) {
            imageCompressListener.onFail(true, imageInstances, new CompressException("fail to create the output directory"));
            return;
        }
        imageCompressListener.onStart();
        compressIndex = 0;
        //多线程压缩
        for (final ImageInstance imageInstance : imageInstances) {
            SystemOut.println("ImageCompressor ===>循环遍历");
            ThreadPoolManager.getInstance().addTask(new Runnable() {
                @Override
                public void run() {
                    switch (compressConfig.getCompressType()) {
                        case CompressConfig.TYPE_PIXEL:
                            //仅像素压缩
                            compressPixel(imageInstance, compressConfig);
                            break;
                        case CompressConfig.TYPE_QUALITY:
                            //仅质量压缩
                            compressQuality(imageInstance, compressConfig);
                            break;
                        case CompressConfig.TYPE_PIXEL_AND_QUALITY:
                            //像素压缩+质量压缩
                            //先进行像素压缩
                            // 然后再对像素压缩过的图片进行质量压缩
                            //先将需要质量压缩的图片路径改为像素压缩后的图片输出路径，然后进行质量压缩
                            compressPixel(imageInstance, compressConfig);
                            break;
                    }
                    compressIndex++;
                    if (compressIndex >= imageInstances.size()) {

                        //全部压缩操作结束
                        //是否删除源文件
                        if (!compressConfig.isKeepSource()) {
                            for (ImageInstance instance : imageInstances) {
                                deleteFile(instance.getInputPath());
                            }
                        }
                        //通知主线程
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                imageCompressListener.onSuccess(imageInstances);
                            }
                        });
                    }
                }
            });
        }
    }

    private void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    /**
     * 将压缩过的图片保存到指定输出目录
     *
     * @param outputPath 输出路径
     * @param bos        压缩过的图片流数据
     */
    private void saveCompressedImage(String outputPath, ByteArrayOutputStream bos) {
        FileOutputStream fos;//将压缩后的图片保存的本地上指定路径中
        try {
            fos = new FileOutputStream(new File(outputPath));
            fos.write(bos.toByteArray());
            fos.flush();
            fos.close();
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 检查输出路径是否存在
     *
     * @param outPutPath 绝对路径
     */
    private boolean checkOutPutPath(String outPutPath) {
        boolean isOuPutDirExist;
        File file = new File(outPutPath);
        if (!file.exists()) {
            isOuPutDirExist = file.mkdirs();
        } else {
            isOuPutDirExist = true;
        }
        return isOuPutDirExist;
    }

    /**
     * 质量压缩
     */
    private void compressQuality(ImageInstance imageInstance, CompressConfig compressConfig) {
        SystemOut.println("ImageCompressor ===>compressQuality()");
        Bitmap inputBitmap = BitmapFactory.decodeFile(imageInstance.getInputPath());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int quality = 90;
        inputBitmap.compress(compressConfig.getComPressFormat(), quality, byteArrayOutputStream);
        //如果压缩后图片还是>targetSize，则继续压缩
        while (byteArrayOutputStream.toByteArray().length > compressConfig.getTargetSize()) {
            byteArrayOutputStream.reset();
            quality -= 10;
            if (quality <= 10) {//限制最低压缩到5
                quality = 5;
            }
            inputBitmap.compress(compressConfig.getComPressFormat(), quality, byteArrayOutputStream);
            if (quality == 5) {
                inputBitmap.recycle();
                break;
            }
        }
        String outputPath;
        if (compressConfig.getCompressType() == CompressConfig.TYPE_PIXEL_AND_QUALITY) {
            outputPath = imageInstance.getOutPutPath();
        } else {
            outputPath = imageInstance.getOutPutPath() + getFileName(imageInstance.getInputPath()) + compressConfig.getFileSuffix();
        }
        saveCompressedImage(outputPath, byteArrayOutputStream);
    }

    /**
     * 像素压缩
     * 根据采样率忽略一部分像素达到压缩的目的
     */
    private void compressPixel(ImageInstance imageInstance, CompressConfig compressConfig) {
        SystemOut.println("ImageCompressor ===>compressPixel()");

        BitmapFactory.Options options = new BitmapFactory.Options();
        //BitmapFactory.decodeFile()源码中介绍Option的内容如下:
        //     @param opts null-ok; Options that control downsampling and whether the
        //                image should be completely decoded, or just is size returned.
        // 设置options.inJustDecodeBounds会返回整张图片或者图片的size
        //计算采样率只需要宽高
        options.inJustDecodeBounds = true;
        //此处在option中已取得宽高
        BitmapFactory.decodeFile(imageInstance.getInputPath(), options);
        //设置采样率
        options.inSampleSize = calculateSampleSize(options, compressConfig);

        //重新设置为decode整张图片，准备压缩
        options.inJustDecodeBounds = false;

        options.inPreferredConfig = Bitmap.Config.ARGB_8888; // 即使不设置，也会默认采用改模式

        // 以下两项，4.4以下生效，当系统内存不够时候图片自动被回收
        options.inPurgeable = true;
        options.inInputShareable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imageInstance.getInputPath(), options);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        bitmap.recycle();

        String outputPath = imageInstance.getOutPutPath() + getFileName(imageInstance.getInputPath()) + compressConfig.getFileSuffix();
        imageInstance.setOutPutPath(outputPath);
        saveCompressedImage(outputPath, byteArrayOutputStream);

        //还需要质量压缩
        if (compressConfig.getCompressType() == CompressConfig.TYPE_PIXEL_AND_QUALITY) {
            String originalImagePath = imageInstance.getInputPath();

            imageInstance.setInputPath(outputPath);
            //此时质量压缩的源文件路径应该为像素压缩的输出路径
            compressQuality(imageInstance, compressConfig);
            //质量压缩完成，恢复原始图片输入路径
            imageInstance.setInputPath(originalImagePath);
        }

    }

    /**
     * 计算出所需要压缩的大小
     */
    private int calculateSampleSize(BitmapFactory.Options options, CompressConfig compressConfig) {
        int sampleSize = 1;
        int picWidth = options.outWidth;
        int picHeight = options.outHeight;
        int maxPixel = compressConfig.getMaxPixel();

        // 缩放比,用高或者宽其中较大的一个数据进行计算
        if (picWidth >= picHeight && picWidth > maxPixel) {
            sampleSize = picWidth / maxPixel;
            sampleSize++;
        } else if (picWidth < picHeight && picHeight > maxPixel) {
            sampleSize = options.outHeight / maxPixel;
            sampleSize++;
        }
        return sampleSize;
    }

    /**
     * 从文件路径中提取文件名
     */
    private String getFileName(String path) {
        int start = path.lastIndexOf("/");
        int end = path.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return path.substring(start + 1, end);
        } else {
            return null;
        }
    }
}
