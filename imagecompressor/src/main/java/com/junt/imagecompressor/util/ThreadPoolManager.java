package com.junt.imagecompressor.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {

    private LinkedBlockingQueue<Runnable> queue=new LinkedBlockingQueue<>();
    private static volatile ThreadPoolManager threadPoolManager;
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 核心线程，不停从队列中取出任务并执行
     */
    private Runnable coreThread=new Runnable() {
        Runnable runnable;
        @Override
        public void run() {
            while (true){
                try {
                    runnable=queue.take();
                    threadPoolExecutor.execute(runnable);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public static ThreadPoolManager getInstance(){
        if (threadPoolManager==null){
            synchronized (ThreadPoolManager.class){
                if (threadPoolManager==null){
                    threadPoolManager=new ThreadPoolManager();
                }
            }
        }
        return threadPoolManager;
    }

    public ThreadPoolManager() {
        //实例化线程池
        threadPoolExecutor=new ThreadPoolExecutor(
                3,
                5,
                15,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(4), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                addTask(r);
            }
        });
        //执行核心线程
        threadPoolExecutor.execute(coreThread);
    }

    public void addTask(Runnable runnable){
        if (runnable==null){
            return;
        }
        try {
            queue.put(runnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
