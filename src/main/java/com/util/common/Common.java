package com.util.common;

import java.lang.reflect.Method;
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
	
	/*
	 * 用来方便测试私有方法
	 * */
	public static Method getPrivateMethod(Object obj, String methodName, Class<?>... parameterTypes) {
		try {
			Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
			method.setAccessible(true);
			return method;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("get private method error!");
		} 
	}
	
	public static Random random() {
		return ThreadLocalRandom.current();
	}
	
	/*
	 * ThreadLocalStringBuilder
	 * */
	private static final ThreadLocal<StringBuilder> localBuilder = 
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
