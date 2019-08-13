package harchiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;

import static harchiver.FileUtilities.*;
import static harchiver.Converters.*;

public class Packer {

    private HTableCreator tableCreator;
    private Map<String, String> htable;

    public Packer() {
        tableCreator = new HTableCreator();
    }

    public void pack(File inputFile) throws Exception {
        //Первый этап - проверка файла
        checkFile(inputFile);

        //Второй этап - получение таблицы Хаффмана
        htable = tableCreator.createTable(inputFile);

        //Третий этап - файловые операции
        File outputFile = createNameOutputFile(inputFile);
        try (FileChannel inputChannel = new FileInputStream(inputFile).getChannel();
             FileChannel outputChannel = new FileOutputStream(outputFile).getChannel()) {

            //Формируем заголовок архива
            createFileHeader(outputChannel, getFileExtension(inputFile));

            //Запаковываем данные
            createFileData(inputChannel, outputChannel);
        } catch (Exception e) {
            throw new Exception("Не удалось создать архив");
        }
    }

    private void checkFile(File file) throws Exception {
        if (!file.exists()) throw new Exception("Файл не существует");
        if (file.length() == 0) throw new Exception("Файл пуст");
        if (!file.canRead()) throw new Exception("Файл не доступен для чтения");
    }

    private void createFileHeader(FileChannel outputChannel, String inputFileExtension) throws IOException {
        //Определяем количество записей в таблице Хаффмана
        int recordCount = htable.size();

        //Определяем длину каждой записи
        int maxCodeSize = 0;
        for (String code : htable.values()) {
            maxCodeSize = Math.max(maxCodeSize, code.length());
        }
        int recordLength = 2 + (maxCodeSize / 8) + ((maxCodeSize % 8) == 0 ? 0 : 1);

        //Записываем эти сведения в архив
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put((byte) recordCount);
        buffer.put((byte) recordLength);
        buffer.flip();
        outputChannel.write(buffer);
        buffer.clear();

        //Вносим в заголовок записи таблицы
        byte key;               //Ключ
        byte lengthValue;       //Длина кода Хаффмана в битах
        String strValue;        //Код Хаффмана для данного ключа
        for (Map.Entry<String, String> entry : htable.entrySet()) {
            //Помещаем в буфер отдельные компоненты записи
            key = convertStringToByte(entry.getKey());
            strValue = entry.getValue();
            lengthValue = (byte) strValue.length();

            while (strValue.length() < ((recordLength - 2) * 8)) {
                strValue += "0";
            }

            buffer.put(key);
            buffer.put(lengthValue);
            for (int i = 0; i < strValue.length(); i += 8) {
                buffer.put(convertStringToByte(strValue.substring(i, i + 8)));
            }

            //Сбрасываем содержимое буфера на диск
            buffer.flip();
            outputChannel.write(buffer);
            buffer.clear();
        }

        //Вносим в заголовок предыдущее расширение файла и его длину
        int extensionLength = inputFileExtension.getBytes().length;
        buffer.put((byte) extensionLength);
        if (extensionLength != 0) {
            buffer.put(inputFileExtension.getBytes());
        }
        buffer.flip();
        outputChannel.write(buffer);
    }

    private void createFileData(FileChannel inputChannel, FileChannel outputChannel) throws IOException {
        int sizeBuffer = 1024;
        ByteBuffer buffer = ByteBuffer.allocate(sizeBuffer);    //Буфер для хранения прочитанных байт из файла-источника
        StringBuffer hBuffer = new StringBuffer();              //Буфер для хранения кодов, которые будут записаны в файл-приёмник
        String key;                                             //Ключ для поиска кода в таблице Хаффмана
        String code;                                            //Код, полученный из таблицы Хаффмана
        int readBytes;                                          //Количество байт, прочитанных из файла в буфер
        int writeBytes;                                         //Количество байт, записанных в буфер

        while (true) {
            readBytes = inputChannel.read(buffer);

            if (readBytes != (-1)) {
                for (int i = 0; i < readBytes; i++) {
                    key = convertByteToString(buffer.get(i));
                    code = htable.get(key);
                    hBuffer.append(code);
                }

                //Сбрасываем на диск содержимое hBuffer
                buffer.clear();
                writeBytes = 0;
                while (hBuffer.length() >= 8) {
                    buffer.put(convertStringToByte(hBuffer.substring(0, 8)));
                    writeBytes++;
                    hBuffer.delete(0, 8);
                    if (writeBytes == sizeBuffer | hBuffer.length() < 8) {
                        buffer.flip();
                        outputChannel.write(buffer);
                        buffer.clear();
                        writeBytes = 0;
                    }
                }
            } else {
                //Сбрасываем на диск содержимое hBuffer и при этом формируем значение конечного байта файла
                int finalValue = hBuffer.length();
                while (hBuffer.length() < 8) {
                    hBuffer.append('0');
                }
                buffer.put(convertStringToByte(hBuffer.substring(0, 8)));
                buffer.put((byte) finalValue);
                break;
            }
        }
    }

    private File createNameOutputFile(File file) {
        String nameFile = getFileName(file);
        return new File(file.getParent(), nameFile + ".lsa");
    }

}
