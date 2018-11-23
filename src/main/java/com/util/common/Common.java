package com.util.common;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Common {
	@SuppressWarnings("restriction")
	public static sun.misc.Unsafe getUnsafe() {
        try {
            java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (sun.misc.Unsafe) unsafeField.get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
	
	public static Random random() {
		return ThreadLocalRandom.current();
	}
	
	/*
	 * ThreadLocalStringBuilder
	 * */
	private static ThreadLocal<StringBuilder> localBuilder = 
			new ThreadLocal<StringBuilder>() {
		protected StringBuilder initialValue() {
            return new StringBuilder();
        }
	};
	
	public static StringBuilder stringBuilder() {
		StringBuilder builder = localBuilder.get();
		builder.setLength(0);
		return builder;
	}
}
