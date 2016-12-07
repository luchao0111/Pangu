/**
 *
 */
package com.joindata.inf.common.support.fastdfs.dependency.client.codec;

import java.io.File;

import com.joindata.inf.common.support.fastdfs.dependency.client.FastdfsConstants;
import com.joindata.inf.common.support.fastdfs.dependency.client.FileId;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * @author liulongbiao
 */
public class FileAppendEncoder extends FileOperationEncoder {

    private final FileId fileId;

    /**
     * @param fileId
     * @param file
     */
    public FileAppendEncoder(FileId fileId, File file) {
        super(file);
        this.fileId = fileId;
    }

    /**
     * @param fileId
     * @param content
     * @param size
     */
    public FileAppendEncoder(FileId fileId, Object content, long size) {
        super(content, size);
        this.fileId = fileId;
    }

    @Override
    protected byte cmd() {
        return FastdfsConstants.Commands.FILE_APPEND;
    }

    @Override
    protected ByteBuf metadata(ByteBufAllocator alloc) {
        byte[] pathBytes = fileId.pathBytes();

        int metaSize = 2 * FastdfsConstants.FDFS_PROTO_PKG_LEN_SIZE + pathBytes.length;
        ByteBuf buf = alloc.buffer(metaSize);
        buf.writeLong(pathBytes.length);
        buf.writeLong(size());
        buf.writeBytes(pathBytes);
        return buf;
    }
}