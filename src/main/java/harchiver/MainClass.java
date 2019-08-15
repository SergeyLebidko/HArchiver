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
            gui.println("Ошибка при упаковке: " + e);
        }

        gui.println();
        gui.println();
        gui.println("-------------- Начинаем распаковку -------------");
        gui.println();
        gui.println();

        //Тестируем распаковку
        try {
            unpacker.unpack(fileToUnpack);
        } catch (Exception e) {
            gui.println("Ошибка при распаковке: " + e);
        }
    }

}
