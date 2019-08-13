package harchiver;

import java.io.File;

public class FileUtilities {

    //Возвращает расширение файла или пустую строку, если его нет
    public static String getFileExtension(File file) {
        String fileName = file.getName();
        int dotPos = fileName.lastIndexOf(".");
        if ((dotPos == (-1)) | (dotPos == 0) | (dotPos == (fileName.length() - 1))) {
            return "";
        } else {
            return (fileName.substring(dotPos + 1)).toLowerCase();
        }
    }

    //Возвращает имя файла без расширения
    public static String getFileName(File file) {
        String fileName = file.getName();
        int dotPos = fileName.lastIndexOf(".");
        if ((dotPos == (-1)) | dotPos == 0 | (dotPos == (fileName.length() - 1))) {
            return fileName;
        } else {
            return fileName.substring(0, dotPos);
        }
    }

}
