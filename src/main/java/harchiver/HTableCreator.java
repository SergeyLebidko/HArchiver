package harchiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

public class HTableCreator {

    private File file;

    private Map<String, String> htable;    //Таблица Хаффмана. Сопоставляет байты и соответствующие им коды
    private Map<String, Long> freqMap;     //Таблица частот. Сопоставляет байты и частоты, с которыми они встречаются в переданном файле
    private PriorityQueue<Node> queue;     //Приоритетная очередь, на основе которой строится дерево Хаффмана
    private Node root;                     //Корневой узел дерева Хаффмана

    private class Node implements Comparable<Node> {

        private String byteStr;
        private long freq;
        private Node left;
        private Node right;

        public Node(String byteStr, long freq) {
            this.byteStr = byteStr;
            this.freq = freq;
            left = null;
            right = null;
        }

        public Node(long freq) {
            this.freq = freq;
            byteStr = null;
            left = null;
            right = null;
        }

        public String getByteStr() {
            return byteStr;
        }

        public long getFreq() {
            return freq;
        }

        public Node getLeft() {
            return left;
        }

        public void setLeft(Node left) {
            this.left = left;
        }

        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public boolean isLeafNode() {
            return left == null & right == null;
        }

        @Override
        public int compareTo(Node o) {
            if (freq < o.getFreq()) return -1;
            if (freq > o.getFreq()) return 1;
            return 0;
        }

    }

    public HTableCreator() {
        freqMap = new HashMap<>();
        htable = new Hashtable<>();
        queue = new PriorityQueue<>();
    }

    public Map<String, String> createTable(File file) throws Exception {
        this.file = file;
        htable.clear();

        //Первый этап - инициализация таблицы частот
        initFreqMap();

        //Второй этап - заполнение таблицы частот по данным из переданного файла
        fillFreqMap();

        //Третий этап - создание приоритетной очереди
        createPriorityQueue();

        //Четвертый этап - построение дерева Хаффмана
        createHuffmanTree();

        //Пятый этап - построение таблицы Хаффмана
        LinkedList<Integer> list = new LinkedList<>();
        list.add(0);
        createHuffmanTable(root, list);

        return htable;
    }

    private void initFreqMap() {
        freqMap.clear();
        for (int i = 0; i < 256; i++) {
            freqMap.put(convertByteToString((byte) i), (long) 0);
        }
    }

    private void fillFreqMap() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        int readBytes;
        String byteStr;
        long freq;

        try (FileChannel channel = new FileInputStream(file).getChannel()) {
            buffer.clear();

            while ((readBytes = channel.read(buffer)) != (-1)) {
                for (int index = 0; index < readBytes; index++) {
                    byteStr = convertByteToString(buffer.get(index));
                    freq = freqMap.get(byteStr);
                    freq++;
                    freqMap.put(byteStr, freq);
                }
                buffer.clear();
            }
        } catch (IOException e) {
            throw new Exception("Не удалось прочитать данные из файла");
        }
    }

    private void createPriorityQueue() {
        queue.clear();
        for (Map.Entry<String, Long> entry : freqMap.entrySet()) {
            if (entry.getValue() == 0) continue;
            queue.add(new Node(entry.getKey(), entry.getValue()));
        }
    }

    private void createHuffmanTree() {
        Node n1, n2;
        Node nextNode;
        while (queue.size() > 1) {
            n1 = queue.poll();
            n2 = queue.poll();
            nextNode = new Node(n1.getFreq() + n2.getFreq());
            nextNode.setLeft(n1);
            nextNode.setRight(n2);
            queue.add(nextNode);
        }
        root = queue.poll();
    }

    private void createHuffmanTable(Node node, LinkedList<Integer> codeList) {
        //Если мы вошли в листовой узел
        if (node.isLeafNode()) {
            String codeStr = "";
            for (int value : codeList) {
                codeStr += value + "";
            }
            htable.put(node.getByteStr(), codeStr);
            return;
        }

        //Если мы вошли в не листовой узел
        Node nextNode;

        //...переходим влево
        nextNode = node.getLeft();
        codeList.add(0);
        createHuffmanTable(nextNode, codeList);
        codeList.pollLast();

        //...переходим вправо
        nextNode = node.getRight();
        codeList.add(1);
        createHuffmanTable(nextNode, codeList);
        codeList.pollLast();
    }

    //Метод возвращает строковое представление переданного байта. Старшие разряды в случае необходимости дополняются нулями
    private String convertByteToString(byte b) {
        String byteString = Integer.toBinaryString(b);
        if (byteString.length() < 8) {
            byteString = "00000000" + byteString;
        }
        byteString = byteString.substring(byteString.length() - 8);
        return byteString;
    }

}
