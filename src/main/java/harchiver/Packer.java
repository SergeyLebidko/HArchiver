package harchiver;

import java.io.File;
import java.util.Map;

public class Packer {

    private HTableCreator tableCreator;

    public Packer() {
        tableCreator = new HTableCreator();
    }

    public void pack(File file) throws Exception {
        //Первый этап - проверка файла
        checkFile(file);

        //Второй этап - получение таблицы Хаффмана
        Map<String, String> htable = tableCreator.createTable(file);


    }

    //Метод проверяет переданный файл. Он должен существовать, быть не пустым и быть доступным для чтения
    private void checkFile(File file) throws Exception {
        if (!file.exists()) throw new Exception("Файл не существует");
        if (file.length() == 0) throw new Exception("Файл пуст");
        if (!file.canRead()) throw new Exception("Файл не доступен для чтения");
    }

}
