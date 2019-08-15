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

    private GUI gui;

    private Map<String, String> htable;
    private File outputFile;

    public Unpacker() {
        htable = new HashMap<>();
    }

    public void setGui(GUI gui) {
        this.gui = gui;
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
            throw new Exception("Не удалось распаковать архив");
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
        int recordCount = reader.getAsByte();
        int recordLength = reader.getAsByte();

        gui.println("Количество записей:  " + recordCount);
        gui.println("Длина каждой записи: " + recordLength);

        //Читаем таблицу. Ключами становятся коды Хаффмана
        gui.println();
        gui.println("прочитана таблица Хаффмана:");

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

            gui.println(String.format("%-20s", value) + " :: " + key);

            htable.put(value.toString(), key);
        }

        //Читаем расширение и формируем выходное имя для файла
        gui.println();

        int extensionLength = reader.getAsByte();
        gui.println("Длина расширения (байт): " + extensionLength);

        if (extensionLength == 0) {
            outputFile = new File(inputFile.getParent(), getFileName(inputFile));
        } else {
            byte[] extension = new byte[extensionLength];
            for (int i = 0; i < extensionLength; i++) {
                extension[i] = reader.getAsByte();
            }

            gui.println("Расширение: " + new String(extension));

            outputFile = new File(inputFile.getParent(), getFileName(inputFile) + "." + new String(extension));
        }

    }

    private void readFileData(BufferReader reader, BufferWriter writer) throws Exception {
        gui.println();
        gui.println("Секция данных:");

        String value;
        while (true) {
            value = reader.getAsString();
            if (value == null) return;

            gui.println(convertStringToByte(value) + " :: " + value);
        }
    }

}
