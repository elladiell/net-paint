package edu.spbsuai.netpaint.client.ui.net;

import edu.spbsuai.netpaint.protocol.Message;

public interface MessageListener {
    void messageReceived(Message m);
}
