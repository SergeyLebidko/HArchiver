package harchiver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

import static harchiver.Converters.*;

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

    public Byte getAsByte() throws IOException {
        if (list.isEmpty()) {
            readFromFile();
        }
        return list.pollFirst();
    }

    public String getAsString() throws IOException {
        Byte b = getAsByte();
        if (b != null) {
            return convertByteToString(b);
        }
        return null;
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
