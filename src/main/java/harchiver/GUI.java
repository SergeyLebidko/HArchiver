package harchiver;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class GUI {

    private final int w = 650;
    private final int h = 350;

    private Packer packer;
    private Unpacker unpacker;

    private JFrame frm;
    private JTextArea outputArea;

    private JFileChooser packFileChooser;
    private JFileChooser unpackFileChooser;
    private JButton toArchiveBtn;
    private JButton fromArchiveBtn;

    private class ArchiveFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            String extension = getFileExtension(f);
            if (extension == null) return true;
            return extension.equals("lsa");
        }

        @Override
        public String getDescription() {
            return "Архивы lsa";
        }

        //Метод возвращает расширение файла file. Если передана ссылка на каталог - возвращает null
        private String getFileExtension(File file) {
            if (file.isDirectory()) return null;
            String nameFile = file.getName();
            int dotPos = nameFile.lastIndexOf(".");
            if ((dotPos == (-1)) | (dotPos == 0) | (dotPos == (nameFile.length() - 1))) return "";
            return (nameFile.substring(dotPos + 1)).toLowerCase();
        }

    }

    public GUI(Packer packer, Unpacker unpacker) {
        this.packer = packer;
        this.unpacker = unpacker;

        UIManager.put("FileChooser.cancelButtonText", "Отмена");
        UIManager.put("FileChooser.directoryOpenButtonText", "Открыть");

        frm = new JFrame("HArchiver");
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setSize(w, h);
        frm.setResizable(false);
        int xPos = Toolkit.getDefaultToolkit().getScreenSize().width / 2 - w / 2;
        int yPos = Toolkit.getDefaultToolkit().getScreenSize().height / 2 - h / 2;
        frm.setLocation(xPos, yPos);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel btnPane = new JPanel();
        btnPane.setLayout(new FlowLayout(FlowLayout.LEFT));
        toArchiveBtn = new JButton("Запаковать");
        toArchiveBtn.addActionListener(pack);
        fromArchiveBtn = new JButton("Распаковать");
        fromArchiveBtn.addActionListener(unpack);
        btnPane.add(toArchiveBtn);
        btnPane.add(fromArchiveBtn);
        contentPane.add(btnPane, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        outputArea.setEditable(false);
        contentPane.add(new JScrollPane(outputArea), BorderLayout.CENTER);

        packFileChooser = new JFileChooser();
        packFileChooser.setDialogTitle("Выберите файл");

        unpackFileChooser = new JFileChooser();
        unpackFileChooser.setDialogTitle("Выберите файл");
        unpackFileChooser.setFileFilter(new ArchiveFileFilter());

        frm.setContentPane(contentPane);
        frm.setVisible(true);
    }

    private ActionListener pack = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int result = packFileChooser.showDialog(frm, "Выбрать");
            if (result != JFileChooser.APPROVE_OPTION) return;

            File selectedFile = packFileChooser.getSelectedFile();
            try {
                packer.pack(selectedFile);
                print("Файл " + selectedFile.getName() + "успешно упакован");
            } catch (Exception ex) {
                print("Ошибка: " + ex.getMessage());
            }
        }
    };

    private ActionListener unpack = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int result = unpackFileChooser.showDialog(frm, "Выбрать");
            if (result != JFileChooser.APPROVE_OPTION) return;

            File selectedFile = unpackFileChooser.getSelectedFile();
            try {
                unpacker.unpack(selectedFile);
                print("Архив " + selectedFile.getName() + " успешно распакован");
            } catch (Exception ex) {
                print("Ошибка: " + ex.getMessage());
            }
        }
    };

    public void print(String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                outputArea.append(text + "\n");
            }
        });
    }

    public void print() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                outputArea.append("\n");
            }
        });
    }

}
