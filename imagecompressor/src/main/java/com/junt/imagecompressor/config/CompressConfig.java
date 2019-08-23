package com.junt.imagecompressor.config;

import android.graphics.Bitmap;

public class CompressConfig {
    //像素压缩
    public static final int TYPE_PIXEL = 0;
    //质量压缩,慎用！当心OOM
    public static final int TYPE_QUALITY = 1;
    //像素压缩+质量压缩
    public static final int TYPE_PIXEL_AND_QUALITY = 2;

    /**
     * 图片压缩方式
     */
    private int compressType = TYPE_PIXEL_AND_QUALITY;

    /**
     * 图片压缩格式
     */
    private Bitmap.CompressFormat comPressFormat= Bitmap.CompressFormat.JPEG;
    private String fileSuffix=".jpg";

    /**
     * 图片压缩像素模式
     */
    private Bitmap.Config pixelConfig= Bitmap.Config.ARGB_8888;

    /**
     * 图片输出路径
     */
    private String outPutPath = "/storage/emulated/0/Android/data/"+ getClass().getPackage().getName() +"/cache/";

    /**
     * 无论宽高，目标允许的最大像素,启用像素压缩时生效
     */
    private int maxPixel=1280;

    /**
     * 图片压缩的目标大小，单位B（最终图片的大小会小于这个值），启用质量压缩时生效
     */
    private int targetSize = 200 * 1024;

    /**
     * 是否保留源文件
     */
    private boolean keepSource = true;

    /**
     * 获取默认的配置
     */
    public static CompressConfig getDefault() {
        return new CompressConfig();
    }

    public String getOutPutPath() {
        return outPutPath;
    }

    public void setOutPutPath(String outPutPath) {
        this.outPutPath = outPutPath;
    }

    public int getCompressType() {
        return compressType;
    }

    public void setCompressType(int compressType) {
        this.compressType = compressType;
    }

    public int getTargetSize() {
        return targetSize;
    }

    public void setTargetSize(int targetSize) {
        this.targetSize = targetSize;
    }

    public boolean isKeepSource() {
        return keepSource;
    }

    public void setKeepSource(boolean keepSource) {
        this.keepSource = keepSource;
    }

    public int getMaxPixel() {
        return maxPixel;
    }

    public void setMaxPixel(int maxPixel) {
        this.maxPixel = maxPixel;
    }

    public Bitmap.Config getPixelConfig() {
        return pixelConfig;
    }

    public void setPixelConfig(Bitmap.Config pixelConfig) {
        this.pixelConfig = pixelConfig;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public Bitmap.CompressFormat getComPressFormat() {
        return comPressFormat;
    }

    public void setComPressFormat(Bitmap.CompressFormat comPressFormat) {
        this.comPressFormat = comPressFormat;
    }



    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CompressConfig config;

        private Builder() {
            config = new CompressConfig();
        }

        public Builder comPressType(int type) {
            config.setCompressType(type);
            return this;
        }

        public Builder targetSize(int size) {
            config.setTargetSize(size);
            return this;
        }

        public Builder maxPixel(int pixel) {
            config.setMaxPixel(pixel);
            return this;
        }

        public Builder keepSource(boolean keep) {
            config.setKeepSource(keep);
            return this;
        }

        public Builder format(Bitmap.CompressFormat format,Bitmap.Config pixelConfig){
            config.setComPressFormat(format);
            if (format== Bitmap.CompressFormat.JPEG){
                config.setFileSuffix(".jpg");
            }else if (format== Bitmap.CompressFormat.PNG){
                config.setFileSuffix(".png");
            }else {
                config.setFileSuffix(".webp");
            }
            config.setPixelConfig(pixelConfig);
            return this;
        }

        public Builder outputDir(String dirPath){
            config.setOutPutPath(dirPath);
            return this;
        }

        //构建配置
        public CompressConfig build() {

            return config;
        }
    }


}
