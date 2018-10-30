package com.util.offheap;

/**
 * 对ByteBuffer的再包装
 */
public interface ByteBuffer {
    
    
    void get(int position, byte[] destination, int offset, int length);
    
   
    void put(int position, byte[] source, int offset, int length);
    
    
    void free();
}
