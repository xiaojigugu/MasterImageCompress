package com.junt.superimagecompressor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.junt.imagecompressor.ImageCompressManager;
import com.junt.imagecompressor.bean.ImageInstance;
import com.junt.imagecompressor.config.CompressConfig;
import com.junt.imagecompressor.exception.CompressException;
import com.junt.imagecompressor.listener.ImageCompressListener;
import com.junt.imagecompressor.util.FileUtils;
import com.junt.imagecompressor.util.SystemOut;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Intent.ACTION_GET_CONTENT;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

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
     *
     * @param images
     */
    private void compress(List<String> images) {

        long size = 0;
        for (String path : images) {
            size += new File(path).length();
        }

        //配置压缩条件
        CompressConfig compressConfig = CompressConfig
                .builder()
                .keepSource(false) //是否保留源文件
                .comPressType(CompressConfig.TYPE_PIXEL_AND_QUALITY) //压缩方式，分为质量压缩、像素压缩、质量压缩+像素压缩，慎用单独的质量压缩（很容易OOM）！
                //目标长边像素,启用像素压缩有效（eg：原图分辨率:7952 X 5304，压缩后7952最终会小于1280）
                .maxPixel(1280)
                //目标大小200kb以内，启用质量压缩有效
                .targetSize(200 * 1024)
                .format(Bitmap.CompressFormat.WEBP, Bitmap.Config.ARGB_8888) //压缩配置
                .outputDir(getFilesDir().getAbsolutePath() + File.separator + "image_compressed/") //输出目录
                .build();
        final long finalSize = size;
        ImageCompressManager.builder()
                .paths(images)
                .config(compressConfig)
                .listener(new ImageCompressListener() {
                    @Override
                    public void onStart() {
                        SystemOut.println("ImageCompressor ===>开始压缩");
                        tvStart.setText(String.format("源文件总大小=%s", getNetFileSizeDescription(finalSize)));
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

    /**
     * 开始压缩
     *
     * @param view
     */
    public void startCompress(View view) {
        tvStart.setText("源文件总大小");
        tvSuccess.setText("压缩后大小");
        tvTotalTime.setText("耗时");

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");//无类型限制
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 99);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 99 && resultCode == RESULT_OK) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                List<Uri> pathList = new ArrayList<>();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    ClipData.Item item = clipData.getItemAt(i);
                    Uri uri = item.getUri();
                    pathList.add(uri);
                }
                moveImages(pathList);
            }
        }
    }

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
                        compress(objects);
                    }
                });
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
        return String.format(Locale.CHINA, "%.3f秒", second);
    }

}
