package edu.spbsuai.netpaint.client.ui.dialogs;

import edu.spbsuai.netpaint.client.ui.net.ConnectionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectionSettingsDialog extends JDialog {

    private JTextField serverName;
    private JSpinner portSpinner;
    private JLabel serverNameLabel;
    private JLabel lbPort;
    private JButton btnLogin;
    private JButton btnCancel;
    private boolean succeeded;



    public ConnectionSettingsDialog(Frame parent) {
        super(parent, "Connection settings", true);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        serverNameLabel = new JLabel("Server name: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(serverNameLabel, cs);

        serverName = new JTextField(20);
        serverName.setText(ConnectionManager.getServerName());
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(serverName, cs);

        lbPort = new JLabel("Port: ");

        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbPort, cs);



        portSpinner = new JSpinner();
        SpinnerModel model =
                new SpinnerNumberModel(ConnectionManager.getPort(), 1000, 65535, 1);
        portSpinner.setModel(model);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(portSpinner, cs);


        btnLogin = new JButton("Ok");

        btnLogin.addActionListener(e -> {
            ConnectionManager.setServerName(serverName.getText());
            ConnectionManager.setPort((Integer) portSpinner.getValue());
            dispose();
        });
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel bp = new JPanel();
//        statusLabel = new JLabel("Not connected");
//        bp.add(statusLabel);
        bp.add(btnLogin);
        bp.add(btnCancel);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(btnLogin);
    }


    public boolean isSucceeded() {
        return succeeded;
    }

    private void enableControls(boolean enable){
        serverName.setEnabled(enable);
        portSpinner.setEnabled(enable);
        btnLogin.setEnabled(enable);
        btnCancel.setEnabled(enable);
    }

}
