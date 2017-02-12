package edu.spbsuai.netpaint.client.ui.net;

import java.io.IOException;

public interface Autentificator {
    boolean doConnect(String login, String password)  throws IOException;
}
