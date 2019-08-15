package harchiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Map;

import static harchiver.FileUtilities.*;

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

            BufferReader reader = new BufferReader(inputChannel);
            BufferWriter writer = new BufferWriter(outputChannel);

            //Формируем заголовок архива
            createFileHeader(writer, getFileExtension(inputFile));

            //Запаковываем данные
            createFileData(reader, writer);
        } catch (Exception e) {
            throw new Exception("не удалось упаковать файл. Ошибка: " + e);
        }
    }

    private void checkFile(File file) throws Exception {
        if (!file.exists()) throw new Exception("Файл не существует");
        if (file.length() == 0) throw new Exception("Файл пуст");
        if (!file.canRead()) throw new Exception("Файл не доступен для чтения");
    }

    private void createFileHeader(BufferWriter writer, String inputFileExtension) throws IOException {
        //Определяем количество записей в таблице Хаффмана
        int recordCount = htable.size();
        if (recordCount == 256) recordCount = 0;

        //Определяем длину каждой записи
        int maxCodeSize = 0;
        for (String code : htable.values()) {
            maxCodeSize = Math.max(maxCodeSize, code.length());
        }
        int recordLength = 2 + (maxCodeSize / 8) + ((maxCodeSize % 8) == 0 ? 0 : 1);

        //Записываем эти сведения в архив
        writer.put(recordCount);
        writer.put(recordLength);

        //Вносим в заголовок архива записи таблицы Хаффмана, которые будут нужны при распаковке
        String key;                   //Байт - ключ
        String valueHuffmanCode;      //Код Хаффмана для данного байта-ключа
        byte lengthHuffmanCode;       //Длина кода Хаффмана в битах
        for (Map.Entry<String, String> entry : htable.entrySet()) {

            //Пулучаем из таблицы Хаффмана отдельные компоненты записи: ключ и код для него
            key = entry.getKey();
            valueHuffmanCode = entry.getValue();
            lengthHuffmanCode = (byte) valueHuffmanCode.length();

            //Если необходимо - дополняем код нулями справа пока его длина не достигнет целого числа байт
            while (valueHuffmanCode.length() < ((recordLength - 2) * 8)) {
                valueHuffmanCode += "0";
            }

            //Сбрасываем подготовленную запись на диск
            writer.put(key);
            writer.put(lengthHuffmanCode);
            for (int i = 0; i < valueHuffmanCode.length(); i += 8) {
                writer.put(valueHuffmanCode.substring(i, i + 8));
            }
        }

        //Вносим в заголовок предыдущее расширение файла и его длину
        int extensionLength = inputFileExtension.getBytes().length;
        writer.put(extensionLength);
        if (extensionLength != 0) {
            for (byte b : inputFileExtension.getBytes()) {
                writer.put(b);
            }
        }
        writer.forceWrite();
    }

    private void createFileData(BufferReader reader, BufferWriter writer) throws IOException {
        String key;
        String code;
        StringBuffer buffer = new StringBuffer();
        while (true) {
            key = reader.getAsString();
            if (key == null) break;
            code = htable.get(key);

            buffer.append(code);
            while (buffer.length() >= 8) {
                writer.put(buffer.substring(0, 8));
                buffer.delete(0, 8);
            }
        }

        int tileValue = buffer.length();
        while (buffer.length() < 8) {
            buffer.append('0');
        }
        writer.put(buffer.substring(0, 8));
        writer.put(tileValue);

        writer.forceWrite();
    }

    private File createNameOutputFile(File file) {
        String nameFile = getFileName(file);
        return new File(file.getParent(), nameFile + ".lsa");
    }

}
