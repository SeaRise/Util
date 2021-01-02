package com.util.concurrent;

import com.util.common.SerializationUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.UUID;

public class DiskBaseQueue<E> extends AbstractQueue<E> implements AutoCloseable {
    protected final Logger logger = LoggerFactory.getLogger(DiskBaseQueue.class);

    private DiskBaseOption diskBaseOption = new DiskBaseOption();
    private int count = 0;
    private int headFileIndex;
    private File headFile;
    private long headOffset;
    private int tailFileIndex;
    private File tailFile;
    public DiskBaseQueue() {
        this.headFileIndex = 0;
        this.headFile = genFile(headFileIndex);
        this.headOffset = 0L;
        this.tailFileIndex = headFileIndex;
        this.tailFile = headFile;
    }

    @Override
    public int size() {
        return count;
    }

    @Override
    public boolean offer(E e) {
        try ( OutputStream stream = new FileOutputStream(tailFile, true);
              DataOutputStream dataStream = new DataOutputStream(stream)) {
            String clazz = e.getClass().getName();
            dataStream.writeInt(clazz.length());
            dataStream.write(clazz.getBytes(StandardCharsets.UTF_8));
            byte[] data = SerializationUtil.serialize(e);
            dataStream.writeInt(data.length);
            dataStream.write(data);
            count++;
            if (tailFile.length() > diskBaseOption.unitFileSizeThreshold) {
                tailFileIndex++;
                logger.info("spill unit file: " + tailFile);
                tailFile = genFile(tailFileIndex);
            }
            return true;
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    @Override
    public E poll() {
        Pair<Integer, E> res = doGet();
        headOffset += res.getLeft();
        if (headOffset == headFile.length()) {
            FileUtils.deleteQuietly(headFile);
            headOffset = 0L;
            headFileIndex++;
            logger.info("remove unit file: " + headFile);
            headFile = genFile(headFileIndex);
        }
        return res.getRight();
    }

    @Override
    public E peek() {
        return doGet().getRight();
    }

    private Pair<Integer, E> doGet() {
        if (count == 0) {
            return Pair.of(0, null);
        }

        try (FileInputStream stream = new FileInputStream(tailFile);
             FileChannel fileChannel = stream.getChannel();
             DataInputStream dataStream = new DataInputStream(stream)) {
            fileChannel.position(headOffset);
            int clazzLen = dataStream.readInt();
            byte[] bytes = getBytesCache(clazzLen);
            dataStream.readFully(bytes, 0, clazzLen);
            String clazz = new String(bytes, 0, clazzLen, StandardCharsets.UTF_8);
            int dataLen = dataStream.readInt();
            bytes = getBytesCache(dataLen);
            dataStream.readFully(bytes, 0, dataLen);
            E data = (E) SerializationUtil.deserialize(bytes, 0, dataLen, Class.forName(clazz));
            return Pair.of(8 + clazzLen + dataLen, data);
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }
    }

    private byte[] bytesCache = new byte[0];
    private byte[] getBytesCache(int len) {
        if (bytesCache.length < len) {
            bytesCache = new byte[len];
        }
        return bytesCache;
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    private File genFile(int fileIndex) {
        return new File(diskBaseOption.baseDir + diskBaseOption.unitFilePrefix + fileIndex);
    }

    @Override
    public void close() throws Exception {
        FileUtils.deleteDirectory(new File(diskBaseOption.baseDir));
        logger.info("delete base dir: " + diskBaseOption.baseDir);
    }

    private class DiskBaseOption {
        private String baseDir = System.getProperty("java.io.tmpdir") +
                "dbbq_" + UUID.randomUUID().toString() + File.pathSeparatorChar;
        private long unitFileSizeThreshold = 10 * 1024 * 1024L; // 10MB
        private String unitFilePrefix = "dbbq_";
    }
}
