package harchiver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;

import static harchiver.Converters.*;

public class BufferWriter {

    private static final int SIZE_BUFFER = 4096;

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

    public void put(int value) throws IOException {
        if (value < 0 | value > 256)
            throw new IOException("Недопустимо большое значение записываемого числа: " + value);
        put((byte) value);
    }

    public void put(String byteStr) throws IOException {
        if (byteStr.length() != 8) throw new IOException("Недопустимая длина байтовой строки: " + byteStr.length());
        put(convertStringToByte(byteStr));
    }

    public void forceWrite() throws IOException {
        writeToFile();
    }

    private void writeToFile() throws IOException {
        buffer.clear();
        while (!list.isEmpty()) {
            buffer.put(list.pollFirst());
        }
        buffer.flip();
        channel.write(buffer);
    }

}
