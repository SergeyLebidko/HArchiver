package harchiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import static harchiver.FileUtilities.*;
import static harchiver.Converters.*;

public class Unpacker {

    private Map<String, String> htable;
    private File outputFile;

    public Unpacker() {
        htable = new HashMap<>();
    }

    public void unpack(File inputFile) throws Exception {
        //Первый этап - проверка файла
        checkFile(inputFile);

        //Второй этап - файловые операции
        try (FileChannel inputChannel = new FileInputStream(inputFile).getChannel()) {

            //Сперва читаем заголовок архива
            readArchiveHeader(inputChannel, inputFile);

            //Затем - разархивируем данные
            try (FileChannel outputChannel = new FileOutputStream(outputFile).getChannel()) {
                readFileData(inputChannel, outputChannel);
            } catch (Exception e) {
                throw e;
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new Exception("Не удалось распаковать архив");
        }

    }

    private void checkFile(File file) throws Exception {
        if (!file.exists()) throw new Exception("Файл не существует");
        if (file.length() == 0) throw new Exception("Файл пуст");
        if (!file.canRead()) throw new Exception("Файл не доступен для чтения");
        if (!getFileExtension(file).equals("lsa")) throw new Exception("Некорректный тип файла");
    }

    private void readArchiveHeader(FileChannel inputChannel, File inputFile) throws Exception {
        //Получаем информацию о количестве записей в таблице Хаффмана и размере каждой записи
        ByteBuffer buffer = ByteBuffer.allocate(2);
        inputChannel.read(buffer);
        int recordCount = convertByteToInt(buffer.get(0));
        int recordLength = convertByteToInt(buffer.get(1));

        //Читаем таблицу. Ключами становятся коды Хаффмана
        StringBuffer key = new StringBuffer();
        String value;
        int keyLength;
        buffer = ByteBuffer.allocate(recordLength);
        for (int i = 0; i < recordCount; i++) {
            inputChannel.read(buffer);

            value = convertByteToString(buffer.get(0));
            keyLength = convertByteToInt(buffer.get(1));
            key.delete(0, key.length());
            for (int j = 2; j < recordLength; j++) {
                key.append(convertByteToString(buffer.get(j)));
            }
            htable.put(key.substring(0, keyLength), value);

            buffer.clear();
        }

        //Читаем расширение и формируем выходное имя для файла
        buffer = ByteBuffer.allocate(1);
        inputChannel.read(buffer);
        int extensionLength = convertByteToInt(buffer.get(0));
        if (extensionLength == 0) {
            outputFile = new File(inputFile.getParent(), getFileName(inputFile));
        } else {
            buffer = ByteBuffer.allocate(extensionLength);
            inputChannel.read(buffer);
            byte[] extension = new byte[extensionLength];
            for (int i = 0; i < extensionLength; i++) {
                extension[i] = buffer.get(i);
            }
            outputFile = new File(inputFile.getParent(), getFileName(inputFile) + "." + new String(extension));
        }
    }

    private void readFileData(FileChannel inputChannel, FileChannel outputChannel) throws Exception {
        int sizeBuffer = 1024;
        ByteBuffer buffer = ByteBuffer.allocate(sizeBuffer);
        StringBuffer hBuffer = new StringBuffer();
        int readBytes;

        while (true) {
            buffer.clear();
            readBytes = inputChannel.read(buffer);
            if (readBytes == (-1)) break;
            for (int i = 0; i < readBytes; i++) {
                hBuffer.append(convertByteToString(buffer.get(i)));
            }
        }

        int tailValue = convertStringToByte(hBuffer.substring(hBuffer.length() - 8, hBuffer.length()));
        hBuffer.delete(hBuffer.length() - 16 + tailValue, hBuffer.length());

        int writeBytes = 0;
        int pos = 0;
        String key;
        String value;
        buffer.clear();
        while (true) {
            pos++;
            key = hBuffer.substring(0, pos);
            value = htable.get(key);

            if (value != null) {
                buffer.put(convertStringToByte(value));
                writeBytes++;
                hBuffer.delete(0, pos);

                if (writeBytes == sizeBuffer | hBuffer.length() == 0) {
                    buffer.flip();
                    outputChannel.write(buffer);
                    buffer.clear();
                    writeBytes = 0;
                }

                pos = 0;
                if (hBuffer.length() == 0) break;
                System.out.println(hBuffer.length());
            }
        }
    }

}
