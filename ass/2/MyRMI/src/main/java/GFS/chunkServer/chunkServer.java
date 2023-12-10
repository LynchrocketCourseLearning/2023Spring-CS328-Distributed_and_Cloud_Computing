package GFS.chunkServer;

import GFS.Info.ChunkInfoProto;

public interface chunkServer {
    boolean pushChunk(ChunkInfoProto.ChunkInfo chunk, byte[] bytes, String chunkServerName) throws Exception;

    boolean appendChunk(ChunkInfoProto.ChunkInfo chunk, byte[] bytes, String chunkServerName) throws Exception;

    boolean deleteChunk(ChunkInfoProto.ChunkInfo chunk, byte[] bytes, String chunkServerName) throws Exception;

    boolean getChunk(ChunkInfoProto.ChunkInfo chunk) throws Exception;

    boolean backupChunk(ChunkInfoProto.ChunkInfo chunk, String chunkServerName) throws Exception;
}
