package GFS.master.impl;

import GFS.Info.ChunkInfoProto;
import GFS.master.master;
import GFS.utils.SnowFlake;
import myrmi.exception.RemoteException;
import myrmi.server.UnicastRemoteObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class masterImpl extends UnicastRemoteObject implements master {
    // list of all chunkServer
    private final List<String> chunkServerList = new ArrayList<>();
    // map of filename : list of chunk
    private final Map<String, List<ChunkInfoProto.ChunkInfo>> fileToChunkMap = new HashMap<>();

    public masterImpl(int port) throws RemoteException {
        super(port);
    }

    @Override
    public void registerChunkServer(String chunkServerAddr) throws Exception {
        chunkServerList.add(chunkServerAddr);
        fileToChunkMap.put(chunkServerAddr, new ArrayList<>());
    }

    @Override
    public ChunkInfoProto.ChunkInfo addChunk(String fileName, int seq, long size, String hash) throws RemoteException {
        String chunkId = SnowFlake.nextId();
        ChunkInfoProto.ChunkInfo chunk = ChunkInfoProto.ChunkInfo.newBuilder()
                .setChunkId(chunkId)
                .setChunkSize(size)
                .setFileSeq(seq)
                .setHash(hash)
                .build();
        List<ChunkInfoProto.ChunkInfo> chunkList = fileToChunkMap.getOrDefault(fileName, new ArrayList<>());
        chunkList.add(chunk);
        fileToChunkMap.put(fileName, chunkList);
        return null;
    }

    @Override
    public List<ChunkInfoProto.ChunkInfo> getChunkInfos(String fileName) throws RemoteException {
        return fileToChunkMap.getOrDefault(fileName, new ArrayList<>());
    }

    @Override
    public List<String> getFileList() throws Exception {
        return new ArrayList<>(fileToChunkMap.keySet());
    }
}
