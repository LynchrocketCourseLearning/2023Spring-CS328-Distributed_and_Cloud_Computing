package GFS.master;

import GFS.Info.ChunkInfoProto;
import myrmi.exception.RemoteException;

import java.util.List;

public interface master {

    /**
     * ChunkServer注册
     *
     * @param ip ip地址
     * @throws Exception exception
     */
    void registerChunkServer(String ip) throws Exception;

    /**
     * 添加chunk
     *
     * @param fileName 文件名
     * @param seq      序号
     * @param size     文件大小
     * @param hash     hash值
     * @return ChunkInfo
     * @throws RemoteException exception
     */
    ChunkInfoProto.ChunkInfo addChunk(String fileName, int seq, long size, String hash) throws RemoteException;

    /**
     * 根据文件名获取chunk相关信息
     *
     * @param fileName 文件名
     * @return list
     * @throws RemoteException exception
     */
    List<ChunkInfoProto.ChunkInfo> getChunkInfos(String fileName) throws RemoteException;

    /**
     * 获取文件列表
     *
     * @return list
     * @throws Exception exception
     */
    List<String> getFileList() throws Exception;
}
