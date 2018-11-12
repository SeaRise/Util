package com.util.time;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * 缓存时间,减少系统调用
 * */
public class TimeUtil {
	
	private static final ScheduledExecutorService executor = 
			Executors.newSingleThreadScheduledExecutor();
	 
    private static final long tickUnit = 
    		Long.parseLong(System.getProperty("notify.systimer.tick", "50"));
 
    private static volatile long time = System.currentTimeMillis();
 
    private static class TimerTicker implements Runnable {

    	public void run() {
            time = System.currentTimeMillis();
        }
        
    }
 
    static {
        executor.scheduleAtFixedRate(new TimerTicker(), tickUnit, tickUnit, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                executor.shutdown();
            }
        });
    }
 
    /**
     * method that returns currentTimeMillis
     * @return 
     * @see
     */
    public static long currentTimeMillis() {
        return time;
    }
    
    public long toSeconds(long millis) {
        return TimeUnit.MILLISECONDS.toSeconds(millis);
    }

    public long toMinutes(long millis) {
        return TimeUnit.MILLISECONDS.toMinutes(millis);
    }

    public long toHours(long millis) {
        return TimeUnit.MILLISECONDS.toHours(millis);
    }

    public long toDays(long millis) {
        return TimeUnit.MILLISECONDS.toDays(millis);
    }
}
