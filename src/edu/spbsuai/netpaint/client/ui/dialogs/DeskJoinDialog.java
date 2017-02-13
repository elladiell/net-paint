package edu.spbsuai.netpaint.client.ui.dialogs;

import edu.spbsuai.netpaint.client.ui.net.ConnectionManager;
import edu.spbsuai.netpaint.protocol.Message;
import edu.spbsuai.netpaint.protocol.Protocol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class DeskJoinDialog extends JDialog {

    private JComboBox cbDesksNames;
    private final DefaultComboBoxModel model = new DefaultComboBoxModel();
    private JLabel lbDesk;
    private JLabel statusLabel;
    private JButton btnJoin;
    private JButton btnCancel;
    private boolean succeeded;


    private String selectedDesk;
    private String resultMessage;

    public String getSelectedDesk() {
        return selectedDesk;
    }

    public DeskJoinDialog(Frame parent) {
        super(parent, "Desk joining", true);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        lbDesk = new JLabel("Select name of desk: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbDesk, cs);

        cbDesksNames = new JComboBox(model);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(cbDesksNames, cs);

        btnJoin = new JButton("Join desk");

        btnJoin.addActionListener(e -> {
            selectedDesk = cbDesksNames.getSelectedItem().toString();
            DeskJoiner dnc = new DeskJoiner(selectedDesk);
            dnc.execute();
        });
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel bp = new JPanel();
        statusLabel = new JLabel("Desks names are not fetched");
        bp.add(statusLabel);
        bp.add(btnJoin);
        bp.add(btnCancel);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
        getRootPane().setDefaultButton(btnJoin);
        enableControls(false);
        DesksNamesFetcher dnf = new DesksNamesFetcher();
        dnf.execute();
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    private void enableControls(boolean enable) {
        cbDesksNames.setEnabled(enable);
        btnJoin.setEnabled(enable);
        btnCancel.setEnabled(enable);
    }

    public String getResultMessage() {
        return resultMessage;
    }

    private class DesksNamesFetcher extends SwingWorker<Boolean, Void> {

        private String respMessage;
        private java.util.List<String> deskNames = new ArrayList<>();

        public DesksNamesFetcher() {
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            ConnectionManager cm = ConnectionManager.getInstance();
            cm.sendMessage(Protocol.buildRequestDesksList());
            Message m = cm.receiveMessage(Protocol.MessageCodes.RESPONSE_DESKS_LIST);
            int okOrError = (int) m.getParamByIndex(0);
            int countOfDesks = (Integer) m.getParamByIndex(1);
            for (int i = 0; i < countOfDesks; i++) {
                deskNames.add((String) m.getParamByIndex(i + 2));
            }
            return Protocol.ResponseStatuses.values()[okOrError] == Protocol.ResponseStatuses.OK;
        }

        @Override
        protected void done() {

            try {
                if (get()) {
                    for (String deskName : deskNames) {
                        model.addElement(deskName);
                    }
                    if (model.getSize() > 0) {
                        enableControls(true);
                        resultMessage = "Desk names are fetched";
                        statusLabel.setText(resultMessage);
                    } else {
                        resultMessage = "No shared desks now";
                        statusLabel.setText(resultMessage);
                        btnCancel.setEnabled(true);
                    }

                    succeeded = false;
                } else {
                    resultMessage = "Desk names are not fetched";
                    succeeded = false;
                    statusLabel.setText(respMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private class DeskJoiner extends SwingWorker<Boolean, Void> {

        private String deskName;
        private String respMessage;

        public DeskJoiner(String deskName) {
            this.deskName = deskName;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            ConnectionManager cm = ConnectionManager.getInstance();
            cm.sendMessage(Protocol.buildRequestJoinDesk(deskName));
            Message m = cm.receiveMessage(Protocol.MessageCodes.RESPONSE_DESK_JOIN);
            int okOrError = (int) m.getParamByIndex(0);
            respMessage = (String) m.getParamByIndex(1);
            return Protocol.ResponseStatuses.values()[okOrError] == Protocol.ResponseStatuses.OK;
        }

        @Override
        protected void done() {
            enableControls(true);
            try {
                if (get()) {
                    resultMessage = "Joined to  desk:" + deskName;
                    statusLabel.setText("Desk is joined");
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
