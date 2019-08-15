package harchiver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

public class BufferWriter {

    private static final int SIZE_BUFFER = 2048;

    private FileChannel channel;
    private ByteBuffer buffer;
    private LinkedList<Byte> list;

    public BufferWriter(FileChannel channel) {
        this.channel = channel;
        buffer = ByteBuffer.allocate(SIZE_BUFFER);
        list = new LinkedList<>();
    }

    public void put(byte value) throws IOException {
        list.add(value);
        if (list.size() == SIZE_BUFFER) {
            writeToFile();
        }
    }

    public void flush() throws IOException {
        writeToFile();
    }

    private void writeToFile() throws IOException {
        buffer.clear();
        while (!list.isEmpty()) {
            buffer.put(list.pollFirst());
        }
        list.clear();
        buffer.flip();
        channel.write(buffer);
    }

}
