package edu.spbstu.netpaint;

import edu.spbstu.netpaint.layouts.ModifiedFlowLayout;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class NetPaint extends JFrame {

    private class ImageFileFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = Utils.getExtension(f);
            if (extension != null) {
                if (extension.equals(Utils.tiff) ||
                        extension.equals(Utils.tif) ||
                        extension.equals(Utils.gif) ||
                        extension.equals(Utils.jpeg) ||
                        extension.equals(Utils.jpg) ||
                        extension.equals(Utils.png)) {
                    return true;
                } else {
                    return false;
                }
            }

            return false;
        }

        @Override
        public String getDescription() {
            return "Image files";
        }
    }

    private PaintDesk pd = new PaintDesk();
    private JSpinner thicknessSpinner = new JSpinner();
    private JSpinner widthSpinner = new JSpinner();
    private JSpinner heightSpinner = new JSpinner();

    private File openedFile;

    public NetPaint() throws HeadlessException {
        super("NetPaint");
        add(new JScrollPane(pd));
        JToolBar tb = createPaintToolBar();
        add(tb, BorderLayout.NORTH);
        JMenuBar menu = new JMenuBar();
        JMenu m = new JMenu("File");
        JMenuItem miOpen = new JMenuItem("Open");
        miOpen.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        miOpen.addActionListener(e -> {
            try {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new  ImageFileFilter());
                int retVal = fc.showOpenDialog(NetPaint.this);
                if(retVal == JFileChooser.APPROVE_OPTION){
                    File file = fc.getSelectedFile();
                    openedFile = file;
                    pd.setImage(ImageIO.read(openedFile));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        JMenuItem miSave = new JMenuItem("Save");
        miSave.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        miSave.addActionListener(e -> {
            if(openedFile != null){
                BufferedImage bi = (BufferedImage) pd.getImage();
                try {
                    ImageIO.write(bi, Utils.getExtension(openedFile), openedFile);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        JMenuItem miSaveAs = new JMenuItem("Save as");
        miSaveAs.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        miSaveAs.addActionListener(e -> {
            try {
                // retrieve image
                BufferedImage bi = (BufferedImage) pd.getImage();
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new ImageFileFilter());
                int retVal = fc.showSaveDialog(NetPaint.this);
                File outputfile = new File("");
                if(retVal == JFileChooser.APPROVE_OPTION){
                    outputfile = fc.getSelectedFile();
                    openedFile = outputfile;
                    ImageIO.write(bi, Utils.getExtension(outputfile), outputfile);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }


        });

        JMenuItem miExit = new JMenuItem("Exit");
        miExit.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        miExit.addActionListener(e -> System.exit(0));
        m.add(miOpen);
        m.add(miSave);
        m.add(miSaveAs);
        m.add(miExit);
        menu.add(m);
        setJMenuBar(menu);
    }

    private JToolBar createPaintToolBar() {
        JToolBar tb = new JToolBar(JToolBar.HORIZONTAL);
        tb.add(new DrawModeChangeToggleBtn("Eraser", null, 'r', PaintDesk.DrawMode.ERASER, pd));
        tb.add(new DrawModeChangeToggleBtn("Curve", null, 'c', PaintDesk.DrawMode.CURVE, pd));
        tb.add(new DrawModeChangeToggleBtn("Line", null, 'l', PaintDesk.DrawMode.LINE, pd));
        tb.add(new DrawModeChangeToggleBtn("Rect", null, 'r', PaintDesk.DrawMode.RECT, pd));
        tb.add(new DrawModeChangeToggleBtn("Ellipse", null, 'e', PaintDesk.DrawMode.ELLIPSE, pd));

        tb.addSeparator();

        JButton colorChooseButton = new JButton("Choose color");
        colorChooseButton.addActionListener(e -> {
            Color initialColor = pd.getLineColor();
            Color color = JColorChooser.showDialog(null, "Change line color",
                    initialColor);
            if (color != null) {
                colorChooseButton.setBackground(color);
                pd.setLineColor(color);
            }
        });
        colorChooseButton.setBorderPainted(false);
        colorChooseButton.setOpaque(true);
        colorChooseButton.setBackground(pd.getLineColor());
        tb.add(new JLabel("Line color:"));
        tb.add(colorChooseButton);




        JButton colorFillChooseButton = new JButton("Choose fill color");
        colorFillChooseButton.addActionListener(e -> {
            Color initialColor = pd.getFillColor();
            Color color = JColorChooser.showDialog(null, "Change fill color",
                    initialColor);
            if (color != null) {
                colorFillChooseButton.setBackground(color);
                pd.setFillColor(color);
            }
        });
        colorFillChooseButton.setBorderPainted(false);
        colorFillChooseButton.setOpaque(true);
        colorFillChooseButton.setBackground(pd.getFillColor());
        tb.add(new JLabel("Fill color:"));
        tb.add(colorFillChooseButton);


        JLabel thicknessLabel = new JLabel("Thickness:");
        tb.add(thicknessLabel);
        SpinnerModel model =
                new SpinnerNumberModel(1, 1, 100, 1);
        thicknessSpinner.setModel(model);
        thicknessSpinner.addChangeListener(e -> {
            JSpinner mySpinner = (JSpinner)(e.getSource());
            SpinnerNumberModel myModel = (SpinnerNumberModel)(mySpinner.getModel());
            pd.setLineThickness(myModel.getNumber().intValue());
        });
        thicknessSpinner.setMaximumSize(new Dimension(60, 30));
        tb.add(thicknessSpinner);

        tb.addSeparator();

        JLabel widthLabel = new JLabel("Width:");
        tb.add(widthLabel);
        model = new SpinnerNumberModel(pd.DEFAULT_WIDTH, 1, 2000, 10);
        widthSpinner.setModel(model);
        widthSpinner.setMaximumSize(new Dimension(60, 30));
        tb.add(widthSpinner);
        JLabel heightLabel = new JLabel("Height:");
        tb.add(heightLabel);
        model = new SpinnerNumberModel(pd.DEFAULT_HEIGHT, 1, 2000, 10);
        heightSpinner.setModel(model);
        heightSpinner.setMaximumSize(new Dimension(60, 30));
        tb.add(heightSpinner);

        JButton sizeBtn = new JButton("Set canvas size");
        sizeBtn.addActionListener(e -> {
            pd.setDrawZoneDimension(new Dimension((Integer)widthSpinner.getValue(), (Integer)heightSpinner.getValue()));
        });
        tb.add(sizeBtn);
        tb.setAlignmentX(0);
        tb.setLayout(new ModifiedFlowLayout(FlowLayout.LEADING, 3, 3));
        tb.setFloatable(false);
        return tb;
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "NetPaint");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
            NetPaint np = new NetPaint();
            np.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            np.pack();
            np.setVisible(true);
        });
    }
}


class DrawModeChangeToggleBtn extends JToggleButton implements ActionListener {
    private PaintDesk.DrawMode drawMode;
    private PaintDesk pd;
    static private java.util.List<JToggleButton> toggles = new ArrayList<>();

    public DrawModeChangeToggleBtn(String text, Icon icon,
                                   char accelerator, PaintDesk.DrawMode drawMode, PaintDesk pd) {
        super(text, icon);
        this.drawMode = drawMode;
        this.pd = pd;
        this.addActionListener(this);
        toggles.add(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (JToggleButton toggle: toggles) {
            if(toggle != this) toggle.setSelected(false);
        }
        if(this.isSelected()) {
            pd.setDrawMode(drawMode);
        }else{
            this.setSelected(true);
        }
    }
}

