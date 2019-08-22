package com.junt.imagecompressor.listener;

import com.junt.imagecompressor.exception.CompressException;
import com.junt.imagecompressor.bean.ImageInstance;

import java.util.List;

public interface ImageCompressListener {

    void onStart();

    void onSuccess(List<ImageInstance> images);

    /**
     * fail to compress images
     * @param allOrSingle true-All of the images compression is fail / false-One of the images compression is fail
     * @param images the instance of compresses images
     * @param e cause of failure
     */
    void onFail(boolean allOrSingle, List<ImageInstance> images, CompressException e);
}
