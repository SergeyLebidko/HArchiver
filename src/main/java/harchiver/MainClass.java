package harchiver;

public class MainClass {

    public static void main(String[] args) {
        Packer packer = new Packer();
        Unpacker unpacker = new Unpacker();
        GUI gui = new GUI(packer, unpacker);
    }

}
