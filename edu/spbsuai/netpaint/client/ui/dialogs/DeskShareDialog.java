package edu.spbsuai.netpaint.client.ui.dialogs;

import edu.spbsuai.netpaint.client.ui.net.Autentificator;
import edu.spbsuai.netpaint.client.ui.net.ConnectionManager;
import edu.spbsuai.netpaint.protocol.Message;
import edu.spbsuai.netpaint.protocol.Protocol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DeskShareDialog extends JDialog {

    private JTextField tfDesk;
    private JLabel lbDesk;
    private JLabel statusLabel;
    private JButton btnLogin;
    private JButton btnCancel;
    private boolean succeeded;


    String defaultDesk = "defaultDesk";
    private String resultMessage;
    private String selectedName;

    public DeskShareDialog(Frame parent) {
        super(parent, "Desk sharing", true);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        lbDesk = new JLabel("Suggest name for desk you are going to share: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbDesk, cs);

        tfDesk = new JTextField(20);
        tfDesk.setText(defaultDesk);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(tfDesk, cs);

        btnLogin = new JButton("Share desk");

        btnLogin.addActionListener(e -> {
            DeskNameChecker dnc = new DeskNameChecker(tfDesk.getText());
            dnc.execute();
        });
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel bp = new JPanel();
        statusLabel = new JLabel("not shared");
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


    public boolean isSucceeded() {
        return succeeded;
    }

    private void enableControls(boolean enable) {
        tfDesk.setEnabled(enable);
        btnLogin.setEnabled(enable);
        btnCancel.setEnabled(enable);
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public String getSelectedName() {
        return selectedName;
    }


    private class DeskNameChecker extends SwingWorker<Boolean, Void> {

        private String deskName;
        private String respMessage;

        public DeskNameChecker(String deskName) {
            this.deskName = deskName;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            ConnectionManager cm = ConnectionManager.getInstance();
            cm.sendMessage(Protocol.buildRequestShareDesk(deskName));
            Message m = cm.receiveMessage(Protocol.MessageCodes.RESPONSE_DESK_SHARE);
            int okOrError = (int) m.getParamByIndex(0);
            respMessage = (String) m.getParamByIndex(1);
            return Protocol.ResponseStatuses.values()[okOrError] == Protocol.ResponseStatuses.OK;
        }

        @Override
        protected void done() {
            enableControls(true);
            try {
                if (get()) {
                    selectedName = deskName;
                    resultMessage = "Desk is shared by name:" + deskName;
                    statusLabel.setText("Desk is shared");
                    succeeded = true;
                    dispose();
                } else {
                    resultMessage = "Not shared/joined";
                    succeeded = false;
                    statusLabel.setText(respMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
