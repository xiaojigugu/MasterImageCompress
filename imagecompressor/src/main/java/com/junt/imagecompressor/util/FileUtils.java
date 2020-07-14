package com.junt.imagecompressor.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.junt.imagecompressor.bean.ImageInstance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    private static final String TAG = "Compressor.FileUtils";

    /**
     * 将图片拷贝至app私有目录
     *
     * @param uris 图片对应的uri
     * @return 图片绝对路径(私有文件目录)集合
     */
    public static List<String> copyImagesToPrivateDir(Context context, List<Uri> uris) {
        List<String> files = new ArrayList<>();
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        for (Uri uri : uris) {
            if (uri.toString().isEmpty()) {
                continue;
            }
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                File file = new File(context.getFilesDir().getAbsolutePath() + File.separator + System.currentTimeMillis() + ".jpg");
                outputStream = new FileOutputStream(file);
                int length;
                byte[] bytes = new byte[1024];
                while ((length = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, length);
                }
                files.add(file.getAbsolutePath());
                Log.i(TAG, "copyImagesToPrivateDir() --> filePath=" + file.getName() + " done!");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return files;
    }


    /**
     * 清除图片
     *
     * @param paths 图片路径
     */
    public static void clearImages(List<ImageInstance> paths) {
        for (ImageInstance imageInstance : paths) {
            File file = new File(imageInstance.getInputPath());
            if (file.exists()) {
                Log.i(TAG, "delete " + file.getName() + " --> " + file.delete());
            }
        }
    }
}
