package harchiver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI {

    private final int w = 650;
    private final int h = 350;

    private JFrame frm;
    private JTextArea outputArea;

    private JButton toArchiveBtn;
    private JButton fromArchiveBtn;

    public GUI() {
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

        frm.setContentPane(contentPane);
        frm.setVisible(true);
    }

    private ActionListener pack = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            print("Запаковать файл...");
        }
    };

    private ActionListener unpack = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            print("Распаковать файл...");
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
