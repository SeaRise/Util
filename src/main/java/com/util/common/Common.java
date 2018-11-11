package com.util.common;

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
}
