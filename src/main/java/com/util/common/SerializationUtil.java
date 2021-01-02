package com.util.common;

import java.util.Map;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

/**
 * Serialization Util（Based on Protostuff）
 */
public class SerializationUtil {
    private static int schemaCapacity = 100;
    
    private static final Map<Class<?>, Schema<?>> cachedSchema = 
    		new ConcurrentLinkedHashMap.Builder<Class<?>, Schema<?>>()
            .maximumWeightedCapacity(schemaCapacity)
            .build();
    
    private static final Objenesis objenesis = new ObjenesisStd(true);

    private static final ThreadLocal<LinkedBuffer> localBuffer = 
    		new ThreadLocal<LinkedBuffer>() {
    	protected LinkedBuffer initialValue() {
            return LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        }
    };
    
    private SerializationUtil() {
    }

    /*
     * 这里没必要做加锁处理,因为被重复put并不会有任何问题.
     * 唯一的问题应该是put太多会导致内存满了.
     * 用了ConcurrentLinkedHashMap来做lru淘汰,最大数量为schemaCapacity
     * */
    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (null == schema) {
            schema = RuntimeSchema.createFrom(cls);
            if (null != cachedSchema.get(cls) && null != schema) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }

    /**
     * 序列化（对象 -> 字节数组）
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = localBuffer.get();
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化（字节数组 -> 对象）
     */
    public static <T> T deserialize(byte[] data, Class<T> cls) {
        return deserialize(data, 0, data.length, cls);
    }

    public static <T> T deserialize(byte[] data,int offset, int length, Class<T> cls) {
        try {
            T object = objenesis.newInstance(cls);
            Schema<T> schema = getSchema(cls);
            ProtostuffIOUtil.mergeFrom(data, offset, length, object, schema);
            return object;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
