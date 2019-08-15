package harchiver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

public class BufferReader {

    private static final int SIZE_BUFFER = 2048;

    private FileChannel channel;
    private ByteBuffer buffer;
    private LinkedList<Byte> list;

    public BufferReader(FileChannel channel) {
        this.channel = channel;
        buffer = ByteBuffer.allocate(SIZE_BUFFER);
        list = new LinkedList<>();
    }

    public byte get() throws IOException {
        if (list.isEmpty()) {
            readFromFile();
        }
        return list.pollFirst();
    }

    private void readFromFile() throws IOException {
        buffer.clear();
        int readBytes;
        readBytes = channel.read(buffer);
        for (int i = 0; i < readBytes; i++) {
            list.add(buffer.get(i));
        }
    }

}
