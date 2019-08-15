package harchiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

            BufferReader reader = new BufferReader(inputChannel);

            //Сперва читаем заголовок архива
            readArchiveHeader(reader, inputFile);

            //Затем - разархивируем данные
            try (FileChannel outputChannel = new FileOutputStream(outputFile).getChannel()) {
                BufferWriter writer = new BufferWriter(outputChannel);
                readFileData(reader, writer);
            } catch (Exception e) {
                throw e;
            }
        } catch (Exception e) {
            throw new Exception("Не удалось распаковать архив. Ошибка: " + e);
        }

    }

    private void checkFile(File file) throws Exception {
        if (!file.exists()) throw new Exception("Файл не существует");
        if (file.length() == 0) throw new Exception("Файл пуст");
        if (!file.canRead()) throw new Exception("Файл не доступен для чтения");
        if (!getFileExtension(file).equals("lsa")) throw new Exception("Некорректный тип файла");
    }

    private void readArchiveHeader(BufferReader reader, File inputFile) throws Exception {
        //Получаем информацию о количестве записей в таблице Хаффмана и размере каждой записи
        int recordCount = reader.getAsPositiveInt();
        int recordLength = reader.getAsPositiveInt();

        //Читаем таблицу. Ключами становятся коды Хаффмана
        htable.clear();
        String key;
        int lengthValue;
        StringBuffer value = new StringBuffer();

        for (int i = 0; i < recordCount; i++) {
            key = reader.getAsString();

            lengthValue = reader.getAsByte();
            if (lengthValue == 0) lengthValue = 256;

            value.delete(0, value.length());
            for (int j = 0; j < (recordLength - 2); j++) {
                value.append(reader.getAsString());
            }
            value.delete(lengthValue, value.length());

            htable.put(value.toString(), key);
        }

        //Читаем расширение и формируем выходное имя для файла
        int extensionLength = reader.getAsByte();
        if (extensionLength == 0) {
            outputFile = new File(inputFile.getParent(), getFileName(inputFile));
        } else {
            byte[] extension = new byte[extensionLength];
            for (int i = 0; i < extensionLength; i++) {
                extension[i] = reader.getAsByte();
            }
            outputFile = new File(inputFile.getParent(), getFileName(inputFile) + "." + new String(extension));
        }

    }

    private void readFileData(BufferReader reader, BufferWriter writer) throws Exception {
        int tileLenght;
        StringBuffer buffer = new StringBuffer();
        String b1 = reader.getAsString();
        String b2 = reader.getAsString();
        String b3 = reader.getAsString();
        String key;
        String value;
        int pos;

        while (true) {

            //Вносим данные в промежуточный буфер
            if (b3 == null) {
                tileLenght = convertStringToByte(b2);
                buffer.append(b1, 0, tileLenght);
            } else {
                buffer.append(b1);
            }

            //Читаем промежуточный буфер и записываем данные в выходной поток
            pos = 1;
            while (pos <= buffer.length()) {
                key = buffer.substring(0, pos);
                value = htable.get(key);
                if (value != null) {
                    writer.put(value);
                    buffer.delete(0, pos);
                    pos = 1;
                } else {
                    pos++;
                }
            }

            if (b3 == null) break;

            //Читаем следующую порцию данных
            b1 = b2;
            b2 = b3;
            b3 = reader.getAsString();
        }

        writer.forceWrite();
    }

}
