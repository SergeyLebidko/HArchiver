package harchiver;

public class Converters {

    //Метод преобразовывает восьмисимвольную строку из символов 0 и 1 в соответствующее ей значение типа byte
    public static byte convertStringToByte(String str) {
        int result = 0;
        int mul = 1;
        for (int i = (str.length() - 1); i >= 0; i--) {
            if (str.charAt(i) == '1') {
                result = result | mul;
            }
            mul *= 2;
        }
        return (byte) result;
    }

    //Метод возвращает строковое представление байта
    public static String convertByteToString(byte b) {
        String byteString = Integer.toBinaryString(b);
        byteString = "0000000" + byteString;
        byteString = byteString.substring(byteString.length() - 8);
        return byteString;
    }

    //Метод преобразовывает переданный байт в целочисленное значение, которое всегда является числом в диапазоне 0..255
    public static int convertByteToInt(byte b) {
        if (b >= 0) return (int) b;
        if (b < 0) return b + 256;
        return 0;
    }

}
