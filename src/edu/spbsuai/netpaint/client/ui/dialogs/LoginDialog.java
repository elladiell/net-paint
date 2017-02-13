package edu.spbsuai.netpaint.client.ui.dialogs;

import edu.spbsuai.netpaint.client.ui.net.ConnectionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginDialog extends JDialog {

    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JLabel lbUsername;
    private JLabel lbPassword;
    private JLabel statusLabel;
    private JButton btnLogin;
    private JButton btnCancel;
    private boolean succeeded;


    String defaultLogin = "guest";
    String defaultPassword = "guest";

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        lbUsername = new JLabel("Username: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbUsername, cs);

        tfUsername = new JTextField(20);
        tfUsername.setText(defaultLogin);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(tfUsername, cs);

        lbPassword = new JLabel("Password: ");

        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbPassword, cs);

        pfPassword = new JPasswordField(20);
        pfPassword.setText(defaultPassword);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(pfPassword, cs);
//        panel.setBorder(new LineBorder(Color.GRAY));

        btnLogin = new JButton("Login");

        btnLogin.addActionListener(e -> {
            ConnectorTask ct = new ConnectorTask(tfUsername.getText(), new String(pfPassword.getPassword()));
            ct.execute();
        });
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel bp = new JPanel();
        statusLabel = new JLabel("Not connected");
        bp.add(statusLabel);
        bp.add(btnLogin);
        bp.add(btnCancel);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(btnLogin);
    }

    public String getUsername() {
        return tfUsername.getText().trim();
    }

    public String getPassword() {
        return new String(pfPassword.getPassword());
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    private void enableControls(boolean enable){
        tfUsername.setEnabled(enable);
        pfPassword.setEnabled(enable);
        btnLogin.setEnabled(enable);
        btnCancel.setEnabled(enable);
    }


    private class ConnectorTask extends SwingWorker<Boolean, Void> {
        private String login, password;

        public ConnectorTask(String login, String password) {
            this.login = login;
            this.password = password;
            enableControls(false);
            statusLabel.setText("Connecting");
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            return ConnectionManager.getInstance().doConnect(login, password);
        }

        @Override
        protected void done() {
            enableControls(true);
            try {
                statusLabel.setText(ConnectionManager.getInstance().getConnectionStatusMessage());
                pack();
                if (get()) {
                    succeeded = true;
                    dispose();
                } else {
                    succeeded = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
