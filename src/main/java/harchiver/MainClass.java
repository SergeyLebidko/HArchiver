package harchiver;

import java.io.File;

public class MainClass {

    public static void main(String[] args) {
        Packer packer = new Packer();
        Unpacker unpacker = new Unpacker();
        GUI gui = new GUI(packer, unpacker);
        packer.setGui(gui);
        unpacker.setGui(gui);

        File fileToPack = new File("sample.txt");
        File fileToUnpack = new File("sample.lsa");

        //Тестируем упаковку
        try {
            packer.pack(fileToPack);
        } catch (Exception e) {
            gui.print("Ошибка при упаковке: " + e);
        }

        //Тестируем распаковку
        try {
            unpacker.unpack(fileToUnpack);
        } catch (Exception e) {
            gui.print("Ошибка при распаковке: " + e);
        }
    }

}
