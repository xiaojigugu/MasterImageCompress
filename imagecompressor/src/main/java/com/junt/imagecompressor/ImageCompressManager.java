package com.junt.imagecompressor;

import com.junt.imagecompressor.bean.ImageInstance;
import com.junt.imagecompressor.config.CompressConfig;
import com.junt.imagecompressor.exception.CompressException;
import com.junt.imagecompressor.listener.ImageCompressListener;
import com.junt.imagecompressor.util.SystemOut;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class ImageCompressManager extends Observable {

    private List<String> originalImages;
    private List<ImageInstance> imageInstanceList;
    private CompressConfig compressConfig;
    private ImageCompressListener imageCompressListener;

    public ImageCompressManager() {
        addObserver(new Compressor());
    }

    public static ImageCompressManager builder() {
        return new ImageCompressManager();
    }

    /**
     * 压缩配置
     */
    public ImageCompressManager config(CompressConfig compressConfig) {
        this.compressConfig = compressConfig;
        return this;
    }

    /**
     * 设置原始图片路径
     */
    public ImageCompressManager paths(List<String> originalImages) {
        this.originalImages = originalImages;
        return this;
    }

    public ImageCompressManager listener(ImageCompressListener imageCompressListener) {
        this.imageCompressListener = imageCompressListener;
        return this;
    }

    /**
     * 条件过滤后进行图片压缩
     */
    public void compress() {
        SystemOut.println("MainActivity ===> compress()");
        //清空压缩图片集合对象
        if (imageInstanceList == null) {
            imageInstanceList = new ArrayList<>(originalImages.size());
        } else {
            imageInstanceList.clear();
        }


        //原始图片路径集合为空，抛出异常
        if (originalImages.size() == 0) {
            imageCompressListener.onFail(true, imageInstanceList, new CompressException("The image set is empty!"));
            return;
        }

        //遍历所有路径
        for (String imagesPath : originalImages) {
            ImageInstance imageInstance = new ImageInstance(imagesPath, compressConfig.getOutPutPath());
            //若输入的文件枯井指向的文件不是文件，抛出异常
            if (isImage(imagesPath)) {
                imageInstanceList.add(imageInstance);
            } else {
                List<ImageInstance> failImageList = new ArrayList<>();
                failImageList.add(imageInstance);
                imageCompressListener.onFail(false, failImageList, new CompressException("This file is not an image"));
            }
        }

        //遍历后没有找到图片路径，抛出异常
        if (imageInstanceList.size() == 0) {
            imageCompressListener.onFail(true, imageInstanceList, new CompressException("filter completely:The image set has no image!"));
            return;
        }

        //开始压缩图片
        setChanged();
        notifyObservers(this);
    }

    /**
     * is the file an image?
     */
    private boolean isImage(String path) {
        boolean is;
        if (path.contains("jpg")
                || path.contains("png")
                || path.contains("webp")) {
            is = true;
        } else {
            is = false;
        }
        return is;
    }

    List<ImageInstance> getImageInstanceList() {
        return imageInstanceList;
    }

    CompressConfig getCompressConfig() {
        return compressConfig;
    }

    ImageCompressListener getImageCompressListener() {
        return imageCompressListener;
    }
}
