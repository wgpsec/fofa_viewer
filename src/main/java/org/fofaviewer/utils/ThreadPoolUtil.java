package org.fofaviewer.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池实现多线程调度
 */
public class ThreadPoolUtil {
    private static ThreadPoolExecutor pool;

    private static ThreadPoolExecutor initPool(){
        if(pool == null){
            synchronized (ThreadPoolUtil.class){
                if(pool == null){
                    pool = new ThreadPoolExecutor(3, 16, 1, TimeUnit.MINUTES, new SynchronousQueue<>(),
                            new ThreadFactory() {
                                private final AtomicInteger integer = new AtomicInteger();
                                @Override
                                public Thread newThread(Runnable r) {
                                    Thread thread = new Thread(r, "RequestPool-" + integer.incrementAndGet());
                                    thread.setDaemon(true);
                                    return thread;
                                }
                            });
                }
            }
        }
        return pool;
    }

    public static Future<?> submit(Runnable runnable){
        return initPool().submit(runnable);
    }

    public static void close(){
        if(pool != null){
            pool.shutdown();
        }
    }
}
