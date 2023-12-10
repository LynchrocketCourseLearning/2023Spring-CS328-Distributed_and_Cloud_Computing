package GFS.chunkServer.impl;

import GFS.Info.ChunkInfoProto;
import GFS.chunkServer.chunkServer;
import myrmi.exception.RemoteException;
import myrmi.server.UnicastRemoteObject;

import java.util.HashMap;
import java.util.Map;

public class chunkServerImpl extends UnicastRemoteObject implements chunkServer {
    private final String chunkServerAddr;
    private final Map<String, String> chunkIdToHashMap = new HashMap<>();

    public chunkServerImpl(String chunkServerAddr) throws RemoteException {
        super();
        this.chunkServerAddr = chunkServerAddr;
    }

    @Override
    public boolean pushChunk(ChunkInfoProto.ChunkInfo chunk, byte[] bytes, String chunkServerName) throws Exception {
        return false;
    }

    @Override
    public boolean appendChunk(ChunkInfoProto.ChunkInfo chunk, byte[] bytes, String chunkServerName) throws Exception {
        return false;
    }

    @Override
    public boolean deleteChunk(ChunkInfoProto.ChunkInfo chunk, byte[] bytes, String chunkServerName) throws Exception {
        return false;
    }

    @Override
    public boolean getChunk(ChunkInfoProto.ChunkInfo chunk) throws Exception {
        return false;
    }

    @Override
    public boolean backupChunk(ChunkInfoProto.ChunkInfo chunk, String chunkServerName) throws Exception {
        return false;
    }
}
