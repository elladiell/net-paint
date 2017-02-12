package edu.spbsuai.netpaint.client.ui;


import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import edu.spbsuai.netpaint.client.ui.dialogs.*;
import edu.spbsuai.netpaint.client.ui.layouts.ModifiedFlowLayout;
import edu.spbsuai.netpaint.client.ui.net.ConnectionManager;
import edu.spbsuai.netpaint.client.ui.net.MessageListener;
import edu.spbsuai.netpaint.protocol.Message;
import edu.spbsuai.netpaint.protocol.Protocol;

public class NetPaint extends JFrame {


    private String joinedDeskName;
    private String sharedByName;

    private PaintPanel pp = new PaintPanel();
    private JSpinner thicknessSpinner = new JSpinner();
    private JSpinner widthSpinner = new JSpinner();
    private JSpinner heightSpinner = new JSpinner();
    private JLabel statusLabel;
    private JLabel statusLabelConnection = new JLabel("Not connected");
    private JLabel statusLabelDeskShared = new JLabel("Not shared");
    private JLabel statusLabelDeskJoined = new JLabel("Not joined");


    private JMenuItem connectionSettingsMenu = new JMenuItem("Connection settings");
    private JMenuItem register = new JMenuItem("Register");
    private JMenuItem share = new JMenuItem("Share");
    private JMenuItem unshare = new JMenuItem("Unshare");
    private JMenuItem join = new JMenuItem("Join");
    private JMenuItem unjoin = new JMenuItem("Unjoin");


    private File openedFile;

    public NetPaint() throws HeadlessException {
        super("NetPaint");
        add(new JScrollPane(pp));
        JToolBar tb = createToolBar();
        add(tb, BorderLayout.NORTH);
        JMenuBar menu = createMenu();
        setJMenuBar(menu);
        pp.addPropertyChangeListener("drawZoneDimension", evt -> {
            Dimension d = (Dimension) evt.getNewValue();
            widthSpinner.setValue(d.getWidth());
            heightSpinner.setValue(d.getHeight());
        });
        JPanel statusPanel = createStatusBar();
        add(statusPanel, BorderLayout.SOUTH);

        ConnectionManager.getInstance().addMessageListener(Protocol.MessageCodes.RESPONSE_DESK_UNJOIN, new MessageListener() {
            @Override
            public void messageReceived(Message m) {
                statusLabelDeskJoined.setText("Unjoined!");
                joinedDeskName = null;
                share.setEnabled(true);
                join.setEnabled(true);
                unjoin.setEnabled(false);
                unshare.setEnabled(false);

            }
        });

        ConnectionManager.getInstance().addMessageListener(Protocol.MessageCodes.RESPONSE_DESK_PAINT, new MessageListener() {
            @Override
            public void messageReceived(Message m) {
                System.out.println("img update from server !!! " + m.getParamByIndex(0));
                pp.setImage((BufferedImage) m.getParamByIndex(1));
            }
        });
        pp.addPropertyChangeListener("image", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("image changed!!");
                if (ConnectionManager.getInstance().isConnected()) {
                    if (sharedByName != null) {
                        try {
                            ConnectionManager.getInstance().sendMessage(Protocol.buildRequestPaint(sharedByName, (BufferedImage) evt.getNewValue()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (joinedDeskName != null) {
                        try {
                            ConnectionManager.getInstance().sendMessage(Protocol.buildRequestPaint(joinedDeskName, (BufferedImage) evt.getNewValue()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });

    }

    private JPanel createStatusBar() {
        pp.addPropertyChangeListener("drawZoneResizing", evt -> {
            Dimension d = (Dimension) evt.getNewValue();
            updateStatusBar(d);
        });
        // create the status bar panel and shove it down the bottom of the frame
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusPanel.setPreferredSize(new Dimension(getWidth(), 16));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusLabel = new JLabel("Status");
        updateStatusBar(pp.getDrawZoneDimension());
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(statusLabelConnection);
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(statusLabelDeskShared);
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(statusLabelDeskJoined);
        return statusPanel;
    }

    private void updateStatusBar(Dimension d) {
        statusLabel.setText("Size: " + d.width + ":" + d.height + "px");
    }

    private JMenuBar createMenu() {
        JMenuBar menu = new JMenuBar();
        JMenu m = new JMenu("File");
        JMenuItem miOpen = new JMenuItem("Open");
        miOpen.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        miOpen.addActionListener(e -> {
            try {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new ImageFileFilter());
                int retVal = fc.showOpenDialog(NetPaint.this);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    openedFile = file;
                    pp.setImage(ImageIO.read(openedFile));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        JMenuItem miSave = new JMenuItem("Save");
        miSave.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        miSave.addActionListener(e -> {
            if (openedFile != null) {
                BufferedImage bi = (BufferedImage) pp.getImage();
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
                BufferedImage bi = (BufferedImage) pp.getImage();
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new ImageFileFilter());
                int retVal = fc.showSaveDialog(NetPaint.this);
                File outputfile = new File("");
                if (retVal == JFileChooser.APPROVE_OPTION) {
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


        JMenu m2 = new JMenu("Net");


        share.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_H, ActionEvent.CTRL_MASK));
        share.addActionListener(e -> {
            if (!ConnectionManager.getInstance().isConnected()) {
                LoginDialog loginDlg = new LoginDialog(NetPaint.this);
                loginDlg.setVisible(true);
                // if logon successfully
                if (loginDlg.isSucceeded()) {
                    statusLabelConnection.setText("Connected as " + loginDlg.getUsername());
                } else {
                    statusLabelConnection.setText("Not connected");
                }
            }
            if (ConnectionManager.getInstance().isConnected()) {
                DeskShareDialog dnDlg = new DeskShareDialog(NetPaint.this);
                dnDlg.setVisible(true);

                sharedByName = dnDlg.getSelectedName();
                try {
                    ConnectionManager.getInstance().sendMessage(Protocol.buildRequestPaint(sharedByName, (BufferedImage) pp.getImage()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                statusLabelDeskShared.setText(dnDlg.getResultMessage());
                if (dnDlg.isSucceeded()) {
                    join.setEnabled(false);
                    unjoin.setEnabled(false);
                    share.setEnabled(false);
                    unshare.setEnabled(true);
                }
            }
        });


        unshare.setEnabled(false);
        unshare.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_U, ActionEvent.CTRL_MASK));
        unshare.addActionListener(e -> {
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    ConnectionManager cm = ConnectionManager.getInstance();
                    cm.sendMessage(Protocol.buildRequestUnshareDesk(sharedByName));
                    Message m = cm.receiveMessage(Protocol.MessageCodes.RESPONSE_DESK_UNSHARE);
                    int okOrError = (int) m.getParamByIndex(0);
                    return Protocol.ResponseStatuses.values()[okOrError] == Protocol.ResponseStatuses.OK;
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            statusLabelDeskShared.setText("Unshared " + sharedByName + " successfully");
                            sharedByName = null;
                            join.setEnabled(true);
                            share.setEnabled(true);
                            unjoin.setEnabled(false);
                            unshare.setEnabled(false);
                        } else {
                            statusLabelDeskShared.setText("Failed to unshare");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.execute();
        });


        join.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_J, ActionEvent.CTRL_MASK));
        join.addActionListener(e -> {
            if (!ConnectionManager.getInstance().isConnected()) {
                LoginDialog loginDlg = new LoginDialog(NetPaint.this);
                loginDlg.setVisible(true);
                // if logon successfully
                if (loginDlg.isSucceeded()) {
                    statusLabelConnection.setText("Connected");
                } else {
                    statusLabelConnection.setText("Not connected");
                }
            }
            if (ConnectionManager.getInstance().isConnected()) {
                DeskJoinDialog dnDlg = new DeskJoinDialog(NetPaint.this);
                dnDlg.setVisible(true);

                joinedDeskName = dnDlg.getSelectedDesk();
                statusLabelDeskJoined.setText(dnDlg.getResultMessage());
                if (dnDlg.isSucceeded()) {
                    share.setEnabled(false);
                    unshare.setEnabled(false);
                    join.setEnabled(false);
                    unjoin.setEnabled(true);
                }
            }
        });
        unjoin.setEnabled(false);
        unjoin.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_K, ActionEvent.CTRL_MASK));
        unjoin.addActionListener(e -> {
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    ConnectionManager cm = ConnectionManager.getInstance();
                    cm.sendMessage(Protocol.buildRequestUnjoinDesk(joinedDeskName));
                    Message m = cm.receiveMessage(Protocol.MessageCodes.RESPONSE_DESK_UNJOIN);
                    int okOrError = (int) m.getParamByIndex(0);
                    return Protocol.ResponseStatuses.values()[okOrError] == Protocol.ResponseStatuses.OK;
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            statusLabelDeskJoined.setText("Not joined");
                            joinedDeskName = null;
                            join.setEnabled(true);
                            share.setEnabled(true);
                            unjoin.setEnabled(false);
                            unshare.setEnabled(false);
                        } else {
                            statusLabelDeskJoined.setText("Failed to unjoined");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.execute();

        });

        register.addActionListener(e -> {
            RegisterDialog regDlg = new RegisterDialog(NetPaint.this);
            regDlg.setVisible(true);
            if (regDlg.isSucceeded()) {
                statusLabelConnection.setText("Connected as " + regDlg.getUsername());
            } else {
                statusLabelConnection.setText("Not connected");
            }
        });
        connectionSettingsMenu.addActionListener(e->{
            ConnectionSettingsDialog c = new ConnectionSettingsDialog(NetPaint.this);
            c.setVisible(true);
        });
        m2.add(connectionSettingsMenu);
        m2.add(register);
        m2.add(share);
        m2.add(unshare);
        m2.add(join);
        m2.add(unjoin);
        menu.add(m2);
        return menu;
    }

    private JToolBar createToolBar() {
        JToolBar tb = new JToolBar(JToolBar.HORIZONTAL);
        tb.add(new DrawModeChangeToggleBtn("Eraser", null, 'r', PaintPanel.DrawMode.ERASER, pp));
        JToggleButton t = new DrawModeChangeToggleBtn("Curve", null, 'c', PaintPanel.DrawMode.CURVE, pp);
        t.setSelected(true);
        tb.add(t);
        tb.add(new DrawModeChangeToggleBtn("Line", null, 'l', PaintPanel.DrawMode.LINE, pp));
        tb.add(new DrawModeChangeToggleBtn("Rect", null, 'r', PaintPanel.DrawMode.RECT, pp));
        tb.add(new DrawModeChangeToggleBtn("Ellipse", null, 'e', PaintPanel.DrawMode.ELLIPSE, pp));

        tb.addSeparator();

        JButton colorChooseButton = new JButton("Choose color");
        colorChooseButton.addActionListener(e -> {
            Color initialColor = pp.getLineColor();
            Color color = JColorChooser.showDialog(null, "Change line color",
                    initialColor);
            if (color != null) {
                colorChooseButton.setBackground(color);
                pp.setLineColor(color);
            }
        });
        colorChooseButton.setBorderPainted(false);
        colorChooseButton.setOpaque(true);
        colorChooseButton.setBackground(pp.getLineColor());
        tb.add(new JLabel("Line color:"));
        tb.add(colorChooseButton);


        JButton colorFillChooseButton = new JButton("Choose fill color");
        colorFillChooseButton.addActionListener(e -> {
            Color initialColor = pp.getFillColor();
            Color color = JColorChooser.showDialog(null, "Change fill color",
                    initialColor);
            if (color != null) {
                colorFillChooseButton.setBackground(color);
                pp.setFillColor(color);
            }
        });
        colorFillChooseButton.setBorderPainted(false);
        colorFillChooseButton.setOpaque(true);
        colorFillChooseButton.setBackground(pp.getFillColor());
        tb.add(new JLabel("Fill color:"));
        tb.add(colorFillChooseButton);


        JLabel thicknessLabel = new JLabel("Thickness:");
        tb.add(thicknessLabel);
        SpinnerModel model =
                new SpinnerNumberModel(1, 1, 100, 1);
        thicknessSpinner.setModel(model);
        thicknessSpinner.addChangeListener(e -> {
            JSpinner mySpinner = (JSpinner) (e.getSource());
            SpinnerNumberModel myModel = (SpinnerNumberModel) (mySpinner.getModel());
            pp.setLineThickness(myModel.getNumber().intValue());
        });
        thicknessSpinner.setMaximumSize(new Dimension(60, 30));
        tb.add(thicknessSpinner);

        tb.addSeparator();

        JLabel widthLabel = new JLabel("Width:");
        model = new SpinnerNumberModel(pp.DEFAULT_WIDTH, 1, 2000, 10);
        widthSpinner.setModel(model);
        widthSpinner.setMaximumSize(new Dimension(60, 30));

        JLabel heightLabel = new JLabel("Height:");
        model = new SpinnerNumberModel(pp.DEFAULT_HEIGHT, 1, 2000, 10);
        heightSpinner.setModel(model);
        heightSpinner.setMaximumSize(new Dimension(60, 30));
        //        tb.add(widthLabel);
//        tb.add(widthSpinner);
        //        tb.add(heightLabel);
//        tb.add(heightSpinner);

        JButton sizeBtn = new JButton("Set canvas size");
        sizeBtn.addActionListener(e -> {
            pp.setDrawZoneDimension(new Dimension((Integer) widthSpinner.getValue(), (Integer) heightSpinner.getValue()));
        });
//        tb.add(sizeBtn);
        tb.setAlignmentX(0);
        tb.setLayout(new ModifiedFlowLayout(FlowLayout.LEADING, 5, 5));
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


}


class DrawModeChangeToggleBtn extends JToggleButton implements ActionListener {
    private PaintPanel.DrawMode drawMode;
    private PaintPanel pd;
    static private java.util.List<JToggleButton> toggles = new ArrayList<>();

    public DrawModeChangeToggleBtn(String text, Icon icon,
                                   char accelerator, PaintPanel.DrawMode drawMode, PaintPanel pd) {
        super(text, icon);
        this.drawMode = drawMode;
        this.pd = pd;
        this.addActionListener(this);
        toggles.add(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (JToggleButton toggle : toggles) {
            if (toggle != this) toggle.setSelected(false);
        }
        if (this.isSelected()) {
            pd.setDrawMode(drawMode);
        } else {
            this.setSelected(true);
        }
    }
}

