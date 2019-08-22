package com.junt.superimagecompressor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.junt.imagecompressor.ImageCompressManager;
import com.junt.imagecompressor.bean.ImageInstance;
import com.junt.imagecompressor.config.CompressConfig;
import com.junt.imagecompressor.exception.CompressException;
import com.junt.imagecompressor.listener.ImageCompressListener;
import com.junt.imagecompressor.util.SystemOut;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvStart, tvSuccess, tvTotalTime;
    private long startTime, endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStart = findViewById(R.id.textStart);
        tvSuccess = findViewById(R.id.textSuccess);
        tvTotalTime = findViewById(R.id.textTotalTime);
    }

    /**
     * 压缩测试
     */
    private void compressTest() {
        File file = new File("/storage/emulated/0/DCIM/Camera/");
        if (file.exists() && file.isDirectory()) {

            File[] files = file.listFiles();
            List<String> images = new ArrayList<>();
            long size = 0;
            for (File file1 : files) {
                size += file1.length();
                String path = file1.getAbsolutePath();
                SystemOut.println("MainActivity ===> image,path=" + path);
                images.add(path);
            }

            //配置压缩条件
            CompressConfig compressConfig = CompressConfig
                    .builder()
                    .keepSource(false) //是否保留源文件
                    .comPressType(CompressConfig.TYPE_PIXEL_AND_QUALITY) //压缩方式，分为质量压缩、像素压缩、质量压缩+像素压缩，慎用单独的质量压缩（很容易OOM）！
                    .maxPixel(2000)  //目标长边像素（eg：原图分辨率:7952 X 5304，压缩后7952最终会小于2000）
                    .targetSize(500 * 1024) //目标大小500kb以内
                    .build();
            final long finalSize = size;
            ImageCompressManager.builder()
                    .paths(images)
                    .config(compressConfig)
                    .listener(new ImageCompressListener() {
                        @Override
                        public void onStart() {
                            SystemOut.println("ImageCompressor ===>开始压缩");
                            tvStart.setText(String.format("原文件大小=%s", getNetFileSizeDescription(finalSize)));
                            startTime = System.currentTimeMillis();
                        }

                        @Override
                        public void onSuccess(List<ImageInstance> images) {
                            SystemOut.println("ImageCompressor ===>压缩成功");
                            endTime = System.currentTimeMillis();

                            long totalSize = 0;
                            for (ImageInstance image : images) {
                                totalSize += new File(image.getOutPutPath()).length();
                            }
                            tvSuccess.setText(String.format("压缩后大小=%s", getNetFileSizeDescription(totalSize)));
                            tvTotalTime.setText(String.format("耗时：%s", getTime(endTime - startTime)));
                        }

                        @Override
                        public void onFail(boolean allOrSingle, List<ImageInstance> images, CompressException e) {
                            SystemOut.println("ImageCompressor ===>压缩失败，isAll=" + allOrSingle);
                        }
                    })
                    .compress();

        }
    }

    /**
     * 开始压缩
     *
     * @param view
     */
    public void startCompress(View view) {
        tvStart.setText("");
        tvSuccess.setText("");
        tvTotalTime.setText("");
        compressTest();
    }


    /**
     * 将byte大小转成kb、mb、gb
     */
    private String getNetFileSizeDescription(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        } else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        } else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B");
            } else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }

    private String getTime(long mill) {
        double second = (double) mill / 1000 + (double) (mill % 1000) / 1000;
        return second + "s ";
    }

}
