package bsu.rfe.java.group6.lab5.Bychkouskaja.var4;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class Main{
    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}

class MainFrame extends JFrame {
    private static final int WIDTH = 700;

    private static final int HEIGHT = 500;

    private JFileChooser fileChooser = null;

    private JMenuItem resetGraphicsMenuItem;

    private GraphicsDisplay display = new GraphicsDisplay();

    private boolean fileLoaded = false;

    public MainFrame() {
        super("Мышь");
        setSize(700, 500);
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation(((kit.getScreenSize()).width - 700) / 2, ((kit.getScreenSize()).height - 500) / 2);
        setExtendedState(6);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);
        Action openGraphicsAction = new AbstractAction("Открыть файл с графиком") {
            public void actionPerformed(ActionEvent event) {
                if (MainFrame.this.fileChooser == null) {
                    MainFrame.this.fileChooser = new JFileChooser();
                    MainFrame.this.fileChooser.setCurrentDirectory(new File("."));
                }
                MainFrame.this.fileChooser.showOpenDialog(MainFrame.this);
                MainFrame.this.openGraphics(MainFrame.this.fileChooser.getSelectedFile());
            }
        };
        fileMenu.add(openGraphicsAction);
        Action resetGraphicsAction = new AbstractAction("Отменить все изменения") {
            public void actionPerformed(ActionEvent event) {
                MainFrame.this.display.reset();
            }
        };
        setJMenuBar(menuBar);
        resetGraphicsMenuItem = fileMenu.add(resetGraphicsAction);
        resetGraphicsMenuItem.setEnabled(false);
        add(display, BorderLayout.CENTER);
    }

    protected void openGraphics(File selectedFile) {
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
            ArrayList<Double[]> graphicsData = (ArrayList) new ArrayList<Double>(50);
            while (in.available() > 0) {
                Double x = Double.valueOf(in.readDouble());
                Double y = Double.valueOf(in.readDouble());
                graphicsData.add(new Double[]{x, y});
            }
            if (graphicsData.size() > 0) {
                this.fileLoaded = true;
                this.resetGraphicsMenuItem.setEnabled(true);
                this.display.displayGraphics(graphicsData);
            }
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Файл не найден","Ошибка загрузки данных",2);
            return;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,"Файл не найден","Ошибка загрузки данных", 2);
            return;
        }
    }
}